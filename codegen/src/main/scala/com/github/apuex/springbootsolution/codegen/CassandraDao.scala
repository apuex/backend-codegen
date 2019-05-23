package com.github.apuex.springbootsolution.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.codegen.ModelUtils._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._
import com.github.apuex.springbootsolution.runtime.TypeConverters._

import scala.xml.{Node, Text}

object CassandraDao extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${cToShell(modelName)}/${cToShell(modelName)}-cassandra-dao"
  val srcDir = s"${projectDir}/src/main/java/${modelPackage.replace('.', '/')}/dao"
  val symboConverter = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}")
    "new IdentityConverter()" else "new CamelToCConverter()"


  new File(srcDir).mkdirs()

  project

  daos(modelPackage, xml)

  def daos(modelPackage: String, xml: Node): Unit = {
    xml.child.filter(x => x.label == "entity")
      .foreach(x => {
        daoForEntity(modelPackage, xml, x)
      })
  }

  def daoForEntity(modelPackage: String, model: Node, entity: Node): Unit = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val prelude =
      s"""package ${modelPackage}.dao;
         |
         |import com.datastax.driver.core.*;
         |import com.datastax.driver.core.exceptions.DriverException;
         |import com.github.apuex.springbootsolution.runtime.*;
         |import static com.github.apuex.springbootsolution.runtime.DateFormat.*;
         |import ${modelPackage}.message.*;
         |import com.github.apuex.springbootsolution.runtime.*;
         |import org.slf4j.*;
         |import org.springframework.beans.factory.annotation.*;
         |import org.springframework.data.cassandra.core.cql.*;
         |import org.springframework.stereotype.*;
         |
         |import java.util.*;
         |
         |@Component
         |public class ${cToPascal(entityName)}DAO {
         |
         |  private final static Logger logger = LoggerFactory.getLogger(${cToPascal(entityName)}DAO.class);
         |  private final WhereClauseWithUnnamedParams where = new WhereClauseWithUnnamedParams(${symboConverter});
         |  private Session session;
         |
         |  @Autowired
         |  public void setSession(Session session) {
         |    this.session = session;
         |  }
         |
         |  ${indent(paramMapper(model, entity), 2)}
         |  private final QueryParamMapper paramMapper = new ParamMapper();
         |  ${indent(rowMapper(model, entity), 2)}
         |  private final RowMapper<${cToPascal(entityName)}Vo> rowMapper = new ResultRowMapper();
         |
         |  public ${cToPascal(entityName)}DAO(Session session) {
         |    this.session = session;
         |  }
         |
         |  public int create(Create${cToPascal(entityName)}Cmd c) {
         |    ${indent(create(model, entity), 4)}
         |  }
         |
         |  public ${cToPascal(entityName)}Vo retrieve(Retrieve${cToPascal(entityName)}Cmd c) {
         |    ${indent(retrieve(model, entity), 4)}
         |  }
         |
         |  public int update(Update${cToPascal(entityName)}Cmd c) {
         |    ${indent(if(isAggregationRoot(entity)) "%s".format(update(model, entity)) else "throw new UnsupportedOperationException();", 4)}
         |  }
         |
         |  public int delete(Delete${cToPascal(entityName)}Cmd c) {
         |    ${indent(delete(model, entity), 4)}
         |  }
         |
         |  public ${cToPascal(entityName)}ListVo query(QueryCommand q) {
         |    final List<${cToPascal(entityName)}Vo> result = new LinkedList<>();
         |    ResultSet rs = query(q, x -> result.add(x));
         |    return ${cToPascal(entityName)}ListVo.newBuilder()
         |        .addAllItems(result)
         |        .setHasMore(!rs.isExhausted())
         |        .setPagingState(rs.getExecutionInfo().getPagingState().toString())
         |        .build();
         |  }
         |
         |  private ResultSet query(QueryCommand q, ResultCallback<${cToPascal(entityName)}Vo> c) {
         |    ${indent(query(model, entity), 4)}
         |  }
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

      s"""Statement statement = new SimpleStatement(\"%s\", %s);
         |session.execute(statement);
         |return 1;""".stripMargin.format(sql, params)
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

    s"""Statement statement = new SimpleStatement(\"%s\", %s);
       |session.execute(statement);
       |return 1;""".stripMargin.format(sql, params)
  }

  private def delete(model: Node, entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val pkFields = primaryKeyFields(entity)

    val pkCriteria = primaryKeyCriteria(entity, pkFields)

    val params = paramsSubstitute(model, entity, (x) => pkFields.contains(x))
      .reduce((x, y) => "%s,%s".format(x, y))

    val sql = "DELETE FROM %s WHERE %s".format(entityName, pkCriteria)

    s"""Statement statement = new SimpleStatement(\"%s\", %s);
       |session.execute(statement);
       |return 1;""".stripMargin.format(sql, params)
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

    s"""Statement statement = new SimpleStatement("${sql}", ${params});
       |ResultSet rs = session.execute(statement);
       |if(!rs.isExhausted())
       |  return rowMapper.mapRow(session.execute(statement).one(), 0);
       |else {
       |  throw new RuntimeException("not found");
       |}""".stripMargin
  }

  private def query(model: Node, entity: Node): String = {
    val entityNames = extendedEntityNames(model, entity)
      .reduce((x, y) => "%s, %s".format(x, y))

    val columns = persistentColumnsExtended(model, entity)
      .map(f => f.\@("name"))
      .reduce((x, y) => "%s, %s".format(x, y))

    s"""String sql = String.format("SELECT ${columns} FROM ${entityNames} %s ", where.toWhereClause(q));
       |logger.info(sql);
       |Statement statement = new SimpleStatement(sql, rowMapper, where.toUnnamedParamList(q, paramMapper).toArray());
       |final int pageSize = q.getRowsPerPage();
       |if(pageSize > 0) statement.setFetchSize(pageSize);
       |if(!q.getPagingState().isEmpty()) statement.setPagingState(PagingState.fromString(q.getPagingState()));
       |ResultSet rs = session.execute(statement);
       |if(pageSize > 0) {
       |  // paginated
       |  for (int i = 0; i < pageSize && !rs.isExhausted(); ++i) {
       |    c.add(rowMapper.mapRow(rs.one(), i));
       |  }
       |} else {
       |  rs.forEach(row -> c.add(rowMapper.mapRow(row, 0)));
       |}
       |return rs;""".stripMargin
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
      .map(f => (f._1, f._2, "row.get%s(\"%s\")".format(cToPascal(toCqlType(f._2)), cToPascal(joinColumns.getOrElse(f._1, f._1)))))
      .map(f => "%sbuilder.set%s(%s);".format(emptyTest(f._2, f._3), cToPascal(joinColumns.getOrElse(f._1, f._1)), convertToColumn(f._2, f._3)))
      .reduce((x, y) => "%s\n    %s".format(x, y))

    val out =
      s"""public static class ResultRowMapper implements RowMapper<${cToPascal(entityName)}Vo> {
         |  public ${cToPascal(entityName)}Vo mapRow(Row row, int rowNum) throws DriverException {
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
         |  <artifactId>${cToShell(modelName)}-cassandra-dao</artifactId>
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
         |      <version>1.0.6</version>
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
         |      <groupId>org.springframework.data</groupId>
         |      <artifactId>spring-data-cassandra</artifactId>
         |      <version>2.1.2.RELEASE</version>
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
