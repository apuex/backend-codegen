package com.github.apuex.springbootsolution.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.codegen.ModelUtils._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TypeConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._

import scala.xml.{Node, Text}

object Dao extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${cToShell(modelName)}/${cToShell(modelName)}-dao"
  val srcDir = s"${projectDir}/src/main/java/${modelPackage.replace('.', '/')}/dao"
  val symboConverter = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}")
    "new IdentityConverter()" else "new CamelToCConverter()"


  new File(srcDir).mkdirs()

  project

  xml.child.filter(x => x.label == "entity")
    .foreach(x => {
      daoForEntity(modelPackage, xml, x)
    })

  def daoForEntity(modelPackage: String, model: Node, entity: Node): Unit = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val prelude =
      s"""package ${modelPackage}.dao;
         |
         |import com.github.apuex.springbootsolution.runtime.*;
         |import static com.github.apuex.springbootsolution.runtime.DateFormat.*;
         |import ${modelPackage}.message.*;
         |import com.github.apuex.springbootsolution.runtime.*;
         |import org.slf4j.*;
         |import org.springframework.beans.factory.annotation.*;
         |import org.springframework.jdbc.core.*;
         |import org.springframework.stereotype.*;
         |
         |import java.sql.*;
         |import java.util.*;
         |
         |@Component
         |public class ${cToPascal(entityName)}DAO {
         |
         |  private final static Logger logger = LoggerFactory.getLogger(${cToPascal(entityName)}DAO.class);
         |  private final WhereClauseWithUnnamedParams where = new WhereClauseWithUnnamedParams(${symboConverter});
         |  @Autowired
         |  private final JdbcTemplate jdbcTemplate;
         |  ${indent(paramMapper(model, entity), 2)}
         |  private final QueryParamMapper paramMapper = new ParamMapper();
         |  ${indent(rowMapper(model, entity), 2)}
         |  private final RowMapper<${cToPascal(entityName)}Vo> rowMapper = new ResultRowMapper();
         |
         |  public ${cToPascal(entityName)}DAO(JdbcTemplate jdbcTemplate) {
         |    this.jdbcTemplate = jdbcTemplate;
         |  }
         |
         |  public int create(Create${cToPascal(entityName)}Cmd c) {
         |    return ${create(model, entity)}
         |  }
         |
         |  public ${cToPascal(entityName)}Vo retrieve(Retrieve${cToPascal(entityName)}Cmd c) {
         |    ${retrieve(model, entity)}
         |  }
         |
         |  public int update(Update${cToPascal(entityName)}Cmd c) {
         |    ${if(isAggregationRoot(entity)) "return %s".format(update(model, entity)) else "throw new UnsupportedOperationException();"}
         |  }
         |
         |  public int delete(Delete${cToPascal(entityName)}Cmd c) {
         |    return ${delete(model, entity)}
         |  }
         |
         |  public List<${cToPascal(entityName)}Vo> query(QueryCommand q) {
         |    ${query(model, entity)}
         |  }
         |
         |""".stripMargin

    val end =
      """}
        |""".stripMargin

    val printWriter = new PrintWriter(s"${srcDir}/${cToPascal(entityName)}DAO.java", "utf-8")

    printWriter.print(prelude)
    printWriter.print(end)

    printWriter.close()
  }

  private def create(model: Node, entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val persistColumns = persistentColumns(entity)
      .filter(f => f.\@("type") != "identity")
    val skipColumns = persistentColumns(entity)
      .filter(f => f.\@("type") == "identity")
      .map(f => f.\@("name"))
      .toSet
    val columns = persistColumns
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .reduce((x, y) => "%s,%s".format(x, y))
    val placeHolders = persistColumns
      .map(_ => "?")
      .reduce((x, y) => "%s,%s".format(x, y))
    val params = paramsSubstitute(model, entity, (x) => !skipColumns.contains(x))
      .reduce((x, y) => "%s,%s".format(x, y))

    val sql = "INSERT INTO %s(%s) VALUES (%s)".format(entityName, columns, placeHolders)

    val out = "jdbcTemplate.update(\"%s\", %s);".format(sql, params)
    out
  }

  private def update(model: Node, entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val pkFields = primaryKeyFields(entity)

    val pkCriteria = primaryKeyCriteria(entity, pkFields)

    val columns = persistentColumns(entity)
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => !pkFields.contains(f))
      .map(f => "%s = ?".format(f))
      .reduce((x, y) => "%s, %s".format(x, y))

    val updates = paramsSubstitute(model, entity, (x) => !pkFields.contains(x))
    val keys = paramsSubstitute(model, entity, (x) => pkFields.contains(x))

    val params = (updates ++ keys)
      .reduce((x, y) => "%s, %s".format(x, y))

    val sql = "UPDATE %s SET %s WHERE %s".format(entityName, columns, pkCriteria)

    val out = "jdbcTemplate.update(\"%s\", %s);".format(sql, params)
    out
  }

  private def delete(model: Node, entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val pkFields = primaryKeyFields(entity)

    val pkCriteria = primaryKeyCriteria(entity, pkFields)

    val params = paramsSubstitute(model, entity, (x) => pkFields.contains(x))
      .reduce((x, y) => "%s,%s".format(x, y))

    val sql = "DELETE FROM %s WHERE %s".format(entityName, pkCriteria)

    val out = "jdbcTemplate.update(\"%s\", %s);".format(sql, params)
    out
  }

  private def paramsSubstitute(model: Node, entity: Node, predicate: String => Boolean) = {
    val joinColumns = joinColumnsForExtension(model, entity)
    persistentColumns(entity)
      .map(f => (f.\@("name"), f.\@("type")))
      .filter(f => predicate(f._1))
      .map(f => ("c.get%s()".format(cToPascal(joinColumns.getOrElse(f._1, f._1))), f._2))
      .map(f => convertFromColumn(f._2, f._1))
  }

  private def primaryKeyCriteria(entity: Node, pkFields: Set[String]): String = {
    persistentColumns(entity)
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => pkFields.contains(f))
      .map(f => "%s = ?".format(f))
      .reduce((x, y) => "%s AND %s".format(x, y))
  }

  private def primaryKeyFields(entity: Node): Set[String] = {
    entity.child.filter(x => x.label == "primaryKey")
      .flatMap(k => k.child.filter(x => x.label == "field"))
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .toSet
  }

  private def retrieve(model: Node, entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val columns = persistentColumnsExtended(model, entity)
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .reduce((x, y) => "%s, %s".format(x, y))

    val pkFields = primaryKeyFields(entity)

    val pkCriteria = primaryKeyCriteria(entity, pkFields)

    val params = paramsSubstitute(model, entity, (x) => pkFields.contains(x))
      .reduce((x, y) => "%s,%s".format(x, y))

    val entityNames = extendedEntityNames(model, entity)
      .reduce((x, y) => "%s, %s".format(x, y))

    val joinPredicate = joinColumnsForExtension(model, entity)
      .map(f => "%s = %s".format(f._1, f._2))
      .foldLeft("")((x, y) => "%s AND %s".format(x, y))

    val sql = s"SELECT %s FROM %s WHERE %s ${joinPredicate}".format(columns, entityNames, pkCriteria)
    val out = "return (%sVo) jdbcTemplate.queryForObject(\"%s\", rowMapper, %s);".format(cToPascal(entityName), sql, params)
    out
  }

  private def query(model: Node, entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val columns = persistentColumnsExtended(model, entity)
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .reduce((x, y) => "%s, %s".format(x, y))

    val entityNames = extendedEntityNames(model, entity)
      .reduce((x, y) => "%s, %s".format(x, y))

    val joinPredicate = joinColumnsForExtension(model, entity)
      .map(f => "%s = %s".format(f._1, f._2))
      .foldLeft("")((x, y) => "%s AND %s".format(x, y))

    val out =
      s"""if(q.getPageNumber() > 0
         |      && q.getRowsPerPage() > 0
         |      && q.getOrderByCount() > 0) {
         |      if(!(q.getOrderByList().stream()
         |        .map(x -> paramMapper.exists(x.getFieldName()))
         |        .reduce((x, y) -> x && y)
         |        .get())) throw new RuntimeException("Invalid order by field.");
         |      String orderBy = q.getOrderByList().stream()
         |          .map(x -> String.format("%s %s", SymbolConverters.cToPascal().apply(x.getFieldName()), x.getOrder()))
         |          .reduce((x, y) -> String.format("%s, %s", x, y))
         |          .get();
         |      String sql = String.format("WITH Paginated${entityName} AS ("
         |          + "SELECT ROW_NUMBER() OVER (ORDER BY %s) AS RowNumber, "
         |          + "${columns} "
         |          + "FROM ${entityNames} %s ${joinPredicate}"
         |          + ")"
         |          + "SELECT ${columns} "
         |          + "FROM Paginated${entityName} "
         |          + "WHERE RowNumber > ? AND RowNumber <= ?",
         |          orderBy,
         |          where.toWhereClause(q));
         |      List<Object> params = new LinkedList<>(where.toUnnamedParamList(q, paramMapper));
         |      params.add(Integer.valueOf((q.getPageNumber() == 0 ? 0 : (q.getPageNumber() - 1)) * q.getRowsPerPage()));
         |      params.add(Integer.valueOf(q.getPageNumber() * q.getRowsPerPage()));
         |      logger.info(sql);
         |      return jdbcTemplate.query(sql, rowMapper, params.toArray());
         |    } else {
         |      String sql = String.format("SELECT ${columns} FROM ${entityNames} %s ${joinPredicate}", where.toWhereClause(q));
         |      logger.info(sql);
         |      return jdbcTemplate.query(sql, rowMapper, where.toUnnamedParamList(q, paramMapper).toArray());
         |    }""".stripMargin
    out
  }

  def convertToColumn(typeName: String, value: String): String = (typeName, value) match {
    case ("bool", v) => v
    case ("short", v) => v
    case ("byte", v) => v
    case ("int", v) => v
    case ("identity", v) => v
    case ("long", v) => v
    case ("decimal", v) => v
    case ("string", v) => v
    case ("timestamp", v) => "toTimestamp(%s)".format(v)
    case ("float", v) => v
    case ("double", v) => v
    case ("blob", v) => "com.google.protobuf.ByteString.copyFrom(%s)".format(v)
    case (t, v) => "%s.forNumber(%s)".format(cToPascal(t), v) // enum type
  }

  def emptyTest(typeName: String, value: String): String = (typeName, value) match {
    case ("bool", v) => ""
    case ("short", v) => ""
    case ("byte", v) => "if(null != %s) ".format(v)
    case ("int", v) => ""
    case ("identity", v) => ""
    case ("long", v) => ""
    case ("decimal", v) => ""
    case ("string", v) => "if(null != %s) ".format(v)
    case ("timestamp", v) => "if(null != %s) ".format(v)
    case ("float", v) => ""
    case ("double", v) => ""
    case ("blob", v) => "if(null != %s) ".format(v)
    case (_, _) => "" // enum type
  }

  def convertFromColumn(typeName: String, value: String): String = (typeName, value) match {
    case ("bool", v) => v
    case ("short", v) => v
    case ("byte", v) => v
    case ("int", v) => v
    case ("identity", v) => v
    case ("long", v) => v
    case ("decimal", v) => v
    case ("string", v) => v
    case ("timestamp", v) => "toDate(%s)".format(v)
    case ("float", v) => v
    case ("double", v) => v
    case ("blob", v) => "%s.toByteArray()".format(v)
    case (_, v) => "%s.getNumber()".format(v) // enum type
  }

  private def rowMapper(model: Node, entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val joinColumns = joinColumnsForExtension(model, entity)
    val columns = persistentColumnsExtended(model, entity)
      .map(f => (f.attribute("name").asInstanceOf[Some[Text]].get.data, f.attribute("type").asInstanceOf[Some[Text]].get.data))
      .map(f => (f._1, f._2, "rs.get%s(\"%s\")".format(cToPascal(toJdbcType(f._2)), cToPascal(joinColumns.getOrElse(f._1, f._1)))))
      .map(f => "%sbuilder.set%s(%s);".format(emptyTest(f._2, f._3), cToPascal(joinColumns.getOrElse(f._1, f._1)), convertToColumn(f._2, f._3)))
      .reduce((x, y) => "%s\n    %s".format(x, y))

    val out =
      s"""public static class ResultRowMapper implements RowMapper<${cToPascal(entityName)}Vo> {
         |  public ${cToPascal(entityName)}Vo mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
         |    ${cToPascal(entityName)}Vo.Builder builder = ${cToPascal(entityName)}Vo.newBuilder();
         |    ${columns}
         |
         |    return builder.build();
         |  }
         |}""".stripMargin
    out
  }


  private def paramMapper(model: Node, entity: Node): String = {
    val columns = persistentColumnsExtended(model, entity)
      .map(f => (f.attribute("name").asInstanceOf[Some[Text]].get.data, f.attribute("type").asInstanceOf[Some[Text]].get.data))
      .map(f => "map.put(\"%s\", TypeConverters.toJavaTypeConverter(\"%s\"))".format(cToCamel(f._1), f._2))
      .reduce((x, y) => "%s;\n    %s".format(x, y))

    val out =
      s"""public static class ParamMapper implements QueryParamMapper {
         |  private final Map<String, TypeConverter> mappers;
         |
         |  public ParamMapper() {
         |    Map<String, TypeConverter> map = new HashMap<>();
         |    ${columns};
         |    this.mappers = map;
         |  }
         |
         |  public Object map(String name, String value) {
         |    TypeConverter c = mappers.get(name);
         |    if(null == c) {
         |      logger.error("No such a field: {}", name);
         |    }
         |    return c.convert(value);
         |  }
         |
         |  public boolean exists(String name) {
         |    return mappers.containsKey(name);
         |  }
         |}""".stripMargin
    out
  }

  private def project = {
    val printWriter = new PrintWriter(s"${projectDir}/pom.xml", "utf-8")

    val source =
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         |         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
         |  <modelVersion>4.0.0</modelVersion>
         |
         |  <groupId>${modelPackage}</groupId>
         |  <artifactId>${cToShell(modelName)}-dao</artifactId>
         |  <version>1.0-SNAPSHOT</version>
         |
         |  <parent>
         |    <groupId>${modelPackage}</groupId>
         |    <artifactId>${cToShell(modelName)}</artifactId>
         |    <version>1.0-SNAPSHOT</version>
         |  </parent>
         |
         |  <dependencies>
         |    <dependency>
         |      <groupId>${modelPackage}</groupId>
         |      <artifactId>${cToShell(modelName)}-message</artifactId>
         |      <version>1.0-SNAPSHOT</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>com.github.apuex.springbootsolution</groupId>
         |      <artifactId>runtime_2.12</artifactId>
         |      <version>1.0.5</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework</groupId>
         |      <artifactId>spring-beans</artifactId>
         |      <version>5.0.7.RELEASE</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework</groupId>
         |      <artifactId>spring-context</artifactId>
         |      <version>5.0.7.RELEASE</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework</groupId>
         |      <artifactId>spring-tx</artifactId>
         |      <version>5.0.7.RELEASE</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework</groupId>
         |      <artifactId>spring-jdbc</artifactId>
         |      <version>5.0.7.RELEASE</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>mysql</groupId>
         |      <artifactId>mysql-connector-java</artifactId>
         |      <version>8.0.11</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>net.sourceforge.jtds</groupId>
         |      <artifactId>jtds</artifactId>
         |      <version>1.3.1</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.slf4j</groupId>
         |      <artifactId>slf4j-api</artifactId>
         |      <version>1.7.25</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework.boot</groupId>
         |      <artifactId>spring-boot-starter-test</artifactId>
         |      <version>2.0.3.RELEASE</version>
         |      <scope>test</scope>
         |    </dependency>
         |  </dependencies>
         |
         |</project>
         |""".stripMargin

    printWriter.print(source)

    printWriter.close()
  }
}
