package com.github.apuex.codegen.springboot

import java.io.{File, PrintWriter}

import com.github.apuex.codegen.runtime.SymbolConverters._
import com.github.apuex.codegen.runtime.TextUtils._

import scala.xml.{Node, Text}

object Dao extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${camelToShell(modelName)}/dao"
  val srcDir = s"${projectDir}/src/main/java/${modelPackage.replace('.', '/')}/dao"

  new File(srcDir).mkdirs()

  project

  xml.child.filter(x => x.label == "entity")
    .foreach(x => {
      daoForEntity(modelPackage, x)
    })

  def daoForEntity(modelPackage: String, entity: Node): Unit = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val prelude =
      s"""package ${modelPackage}.dao;
         |
         |import com.github.apuex.codegen.runtime.*;
         |import ${modelPackage}.message.${camelToPascal(modelName)}.*;
         |import com.github.apuex.codegen.runtime.Messages.*;
         |import org.slf4j.*;
         |import org.springframework.beans.factory.annotation.*;
         |import org.springframework.jdbc.core.*;
         |import org.springframework.stereotype.*;
         |
         |import java.sql.*;
         |import java.util.*;
         |
         |@Component
         |public class ${entityName}DAO {
         |
         |  private final static Logger logger = LoggerFactory.getLogger(${entityName}DAO.class);
         |  private final WhereClauseWithUnnamedParams where = new WhereClauseWithUnnamedParams(new CamelToPascalConverter());
         |  @Autowired
         |  private final JdbcTemplate jdbcTemplate;
         |  ${indent(paramMapper(entity), 2)};
         |  private final QueryParamMapper paramMapper = new ParamMapper();
         |  private final RowMapper rowMapper = ${indent(mapRow(entity), 2)};
         |
         |  public ${entityName}DAO(JdbcTemplate jdbcTemplate) {
         |    this.jdbcTemplate = jdbcTemplate;
         |  }
         |
         |  public void create(Create${entityName}Cmd c) {
         |    ${create(entity)}
         |  }
         |
         |  public ${entityName}Vo retrieve(Retrieve${entityName}Cmd c) {
         |    ${retrieve(entity)}
         |  }
         |
         |  public void update(Update${entityName}Cmd c) {
         |    ${update(entity)}
         |  }
         |
         |  public void delete(Delete${entityName}Cmd c) {
         |    ${delete(entity)}
         |  }
         |
         |  public List<${entityName}Vo> query(QueryCommand q) {
         |    ${query(entity)}
         |  }
         |
         |""".stripMargin

    val end =
      """}
        |""".stripMargin

    val printWriter = new PrintWriter(s"${srcDir}/${entityName}DAO.java", "utf-8")

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
      .map(f => "c.get%s()".format(camelToPascal(f)))
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
      .map(f => "c.get%s()".format(camelToPascal(f)))
    val keys = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .filter(f => pkFields.contains(f))
      .map(f => "c.get%s()".format(camelToPascal(f)))
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
      .map(f => "c.get%s()".format(camelToPascal(f)))
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
      .map(f => "c.get%s()".format(camelToPascal(f)))
      .reduce((x, y) => "%s, %s".format(x, y))

    val sql = "SELECT %s FROM %s WHERE %s".format(columns, entityName, pkCriteria)
    val out = "return (%sVo) jdbcTemplate.queryForObject(\"%s\", rowMapper, %s);".format(entityName, sql, params)
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

  private def mapRow(entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val columns = entity.child.filter(x => x.label == "field")
      .map(f => (f.attribute("name").asInstanceOf[Some[Text]].get.data, f.attribute("type").asInstanceOf[Some[Text]].get.data))
      .map(f => ".set%s(rs.get%s(\"%s\"))".format(camelToPascal(f._1), camelToPascal(f._2), camelToPascal(f._1)))
      .reduce((x, y) => "%s\n      %s".format(x, y))

    val out =
      s"""new RowMapper<${entityName}Vo>() {
         |  public ${entityName}Vo mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
         |    ${entityName}Vo row = ${entityName}Vo.newBuilder()
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
      .map(f => "map.put(\"%s\", TypeConverters.toJavaTypeConverter(\"%s\"))".format(pascalToCamel(f._1), f._2))
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
         |  <artifactId>dao</artifactId>
         |  <version>1.0-SNAPSHOT</version>
         |
         |  <parent>
         |    <groupId>${modelPackage}</groupId>
         |    <artifactId>${camelToShell(modelName)}</artifactId>
         |    <version>1.0-SNAPSHOT</version>
         |  </parent>
         |
         |  <dependencies>
         |    <dependency>
         |      <groupId>${modelPackage}</groupId>
         |      <artifactId>message</artifactId>
         |      <version>1.0-SNAPSHOT</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>com.github.apuex.springboot</groupId>
         |      <artifactId>java-sql-query_2.12</artifactId>
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
