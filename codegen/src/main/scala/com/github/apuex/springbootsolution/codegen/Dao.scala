package com.github.apuex.springbootsolution.codegen

import java.io.{File, PrintWriter}

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
      daoForEntity(modelPackage, x)
    })

  def daoForEntity(modelPackage: String, entity: Node): Unit = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val aggregationRoot = entity.attribute("aggregationRoot").asInstanceOf[Some[Text]].get.data
    val prelude =
      s"""package ${modelPackage}.dao;
         |
         |import com.github.apuex.springbootsolution.runtime.*;
         |import static com.github.apuex.springbootsolution.runtime.DateFormat.*;
         |import ${modelPackage}.message.${cToPascal(modelName)}.*;
         |import com.github.apuex.springbootsolution.runtime.Messages.*;
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
         |  ${indent(paramMapper(entity), 2)};
         |  private final QueryParamMapper paramMapper = new ParamMapper();
         |  private final RowMapper<${cToPascal(entityName)}Vo> rowMapper = ${indent(mapRow(entity), 2)};
         |
         |  public ${cToPascal(entityName)}DAO(JdbcTemplate jdbcTemplate) {
         |    this.jdbcTemplate = jdbcTemplate;
         |  }
         |
         |  public int create(Create${cToPascal(entityName)}Cmd c) {
         |    return ${create(entity)}
         |  }
         |
         |  public ${cToPascal(entityName)}Vo retrieve(Retrieve${cToPascal(entityName)}Cmd c) {
         |    ${retrieve(entity)}
         |  }
         |
         |  public int update(Update${cToPascal(entityName)}Cmd c) {
         |    ${if("true" == aggregationRoot) "return %s".format(update(entity)) else "throw new UnsupportedOperationException();"}
         |  }
         |
         |  public int delete(Delete${cToPascal(entityName)}Cmd c) {
         |    return ${delete(entity)}
         |  }
         |
         |  public List<${cToPascal(entityName)}Vo> query(QueryCommand q) {
         |    ${query(entity)}
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

  private def create(entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val columns = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .reduce((x, y) => "%s,%s".format(x, y))
    val placeHolders = entity.child.filter(x => x.label == "field")
      .map(_ => "?")
      .reduce((x, y) => "%s,%s".format(x, y))
    val params = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .map(f => "c.get%s()".format(cToPascal(f)))
      .reduce((x, y) => "%s,%s".format(x, y))

    val sql = "INSERT INTO %s(%s) VALUES (%s)".format(entityName, columns, placeHolders)

    val out = "jdbcTemplate.update(\"%s\", %s);".format(sql, params)
    out
  }

  private def update(entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val pkFields = entity.child.filter(x => x.label == "primaryKey")
      .flatMap(k => k.child.filter(x => x.label == "field"))
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .toSet

    val columns = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => !pkFields.contains(f))
      .map(f => "%s = ?".format(f))
      .reduce((x, y) => "%s, %s".format(x, y))

    val pkCriteria = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => pkFields.contains(f))
      .map(f => "%s = ?".format(f))
      .reduce((x, y) => "%s AND %s".format(x, y))

    val updates = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => !pkFields.contains(f))
      .map(f => "c.get%s()".format(cToPascal(f)))
    val keys = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => pkFields.contains(f))
      .map(f => "c.get%s()".format(cToPascal(f)))
    val params = (updates ++ keys)
      .reduce((x, y) => "%s, %s".format(x, y))

    val sql = "UPDATE %s SET %s WHERE %s".format(entityName, columns, pkCriteria)

    val out = "jdbcTemplate.update(\"%s\", %s);".format(sql, params)
    out
  }

  private def delete(entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val pkFields = entity.child.filter(x => x.label == "primaryKey")
      .flatMap(k => k.child.filter(x => x.label == "field"))
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .toSet

    val pkCriteria = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => pkFields.contains(f))
      .map(f => "%s = ?".format(f))
      .reduce((x, y) => "%s AND %s".format(x, y))

    val params = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => pkFields.contains(f))
      .map(f => "c.get%s()".format(cToPascal(f)))
      .reduce((x, y) => "%s, %s".format(x, y))

    val sql = "DELETE FROM %s WHERE %s".format(entityName, pkCriteria)

    val out = "jdbcTemplate.update(\"%s\", %s);".format(sql, params)
    out
  }

  private def retrieve(entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val columns = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .reduce((x, y) => "%s, %s".format(x, y))

    val pkFields = entity.child.filter(x => x.label == "primaryKey")
      .flatMap(k => k.child.filter(x => x.label == "field"))
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .toSet

    val pkCriteria = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => pkFields.contains(f))
      .map(f => "%s = ?".format(f))
      .reduce((x, y) => "%s AND %s".format(x, y))

    val params = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => pkFields.contains(f))
      .map(f => "c.get%s()".format(cToPascal(f)))
      .reduce((x, y) => "%s, %s".format(x, y))

    val sql = "SELECT %s FROM %s WHERE %s".format(columns, entityName, pkCriteria)
    val out = "return (%sVo) jdbcTemplate.queryForObject(\"%s\", rowMapper, %s);".format(cToPascal(entityName), sql, params)
    out
  }

  private def query(entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val columns = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .reduce((x, y) => "%s, %s".format(x, y))

    val out =
      s"""String sql = String.format("SELECT ${columns} FROM ${entityName} %s", where.toWhereClause(q));
         |    return jdbcTemplate.query(sql, rowMapper, where.toUnnamedParamList(q, paramMapper).toArray());""".stripMargin
    out
  }

  def convertColumn(typeName: String, value: String): String = (typeName, value) match {
    case ("bool", v) => v
    case ("short", v) => v
    case ("byte", v) => v
    case ("int", v) => v
    case ("long", v) => v
    case ("decimal", v) => v
    case ("string", v) => v
    case ("timestamp", v) => "toTimestamp(%s)".format(v)
    case ("float", v) => v
    case ("double", v) => v
    case ("blob", v) => v
    case (t, v) =>
      throw new IllegalArgumentException("type=%s, value=%s".format(t, v))
  }

  private def mapRow(entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val columns = entity.child.filter(x => x.label == "field")
      .map(f => (f.attribute("name").asInstanceOf[Some[Text]].get.data, f.attribute("type").asInstanceOf[Some[Text]].get.data))
      .map(f => (f._1, f._2, "rs.get%s(\"%s\")".format(cToPascal(toJavaType(f._2)), cToPascal(f._1))))
      .map(f => ".set%s(%s)".format(cToPascal(f._1), convertColumn(f._2, f._3)))
      .reduce((x, y) => "%s\n      %s".format(x, y))

    val out =
      s"""new RowMapper<${cToPascal(entityName)}Vo>() {
         |  public ${cToPascal(entityName)}Vo mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
         |    ${cToPascal(entityName)}Vo row = ${cToPascal(entityName)}Vo.newBuilder()
         |      ${columns}
         |      .build();
         |    return row;
         |  }
         |}""".stripMargin
    out
  }


  private def paramMapper(entity: Node): String = {
    val columns = entity.child.filter(x => x.label == "field")
      .map(f => (f.attribute("name").asInstanceOf[Some[Text]].get.data, f.attribute("type").asInstanceOf[Some[Text]].get.data))
      .map(f => "map.put(\"%s\", TypeConverters.toJavaTypeConverter(\"%s\"))".format(cToCamel(f._1), toJavaType(f._2)))
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
         |    return mappers.get(name).convert(value);
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
         |      <groupId>com.github.apuex.spring-boot-solution</groupId>
         |      <artifactId>runtime_2.12</artifactId>
         |      <version>1.0.0</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework.boot</groupId>
         |      <artifactId>spring-boot-starter-jdbc</artifactId>
         |      <version>2.0.3.RELEASE</version>
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
         |      <groupId>org.springframework.boot</groupId>
         |      <artifactId>spring-boot-starter-test</artifactId>
         |      <version>2.0.3.RELEASE</version>
         |      <scope>test</scope>
         |    </dependency>
         |  </dependencies>
         |
         |</project>
         |
       """.stripMargin

    printWriter.print(source)

    printWriter.close()
  }
}
