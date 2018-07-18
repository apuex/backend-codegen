package com.github.apuex.codegen.springboot

import java.io.{File, PrintWriter}

import com.github.apuex.codegen.runtime.SymbolConverters._
import com.github.apuex.codegen.runtime.TextUtils._

import scala.xml.{Node, Text}

object Dao extends App {
  val xml = ModelLoader(args(0)).xml
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val srcDir = s"dao/src/main/java/${modelPackage.replace('.', '/')}/dao"

  new File(srcDir).mkdirs()

  xml.child.filter(x => x.label == "entity")
    .foreach(x => {
      daoForEntity(modelPackage, x)
    })

  def daoForEntity(modelPackage: String, entity: Node): Unit = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val prelude =
      s"""package ${modelPackage}.dao;
         |
         |import java.util.List;
         |
         |import org.slf4j.Logger;
         |import org.slf4j.LoggerFactory;
         |import org.springframework.jdbc.core.JdbcTemplate;
         |import org.springframework.jdbc.core.RowMapper;
         |import org.springframework.stereotype.Component;
         |import static com.github.apuex.codegen.runtime.Messages.*;
         |import com.github.apuex.codegen.runtime.*;
         |
         |@Component
         |public class ${entityName}DAO {
         |
         |  private final static Logger logger = LoggerFactory.getLogger(${entityName}DAO.class);
         |  private final WhereClauseWithUnnamedParams where = new WhereClauseWithUnnamedParams(SymbolConverters.camelToPascal());
         |  private final JdbcTemplate jdbcTemplate;
         |  private final QueryParamMapper paramMapper = ${indent(paramMapper(entity), 2)};
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
         |  public ${entityName} retrieve(Retrieve${entityName}Cmd c) {
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

    val out = "jdbcTemplate.update(s\"%s\", %s);".format(sql, params)
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

    val out = "jdbcTemplate.update(s\"%s\", %s);".format(sql, params)
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

    val out = "jdbcTemplate.update(s\"%s\", %s);".format(sql, params)
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
    val out = "jdbcTemplate.query(s\"%s\", rowMapper %s);".format(sql, params)
    out
  }

  private def query(entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val columns = entity.child.filter(x => x.label == "field")
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .reduce((x, y) => "%s, %s".format(x, y))

    val out =
      s"""String sql = String.format("SELECT ${columns} FROM ${entityName} %s", where.toWhereClause(q));"
         |      return jdbcTemplate.query(sql, rowMapper, where.toUnnamedParamList(q));""".stripMargin
    out
  }

  private def mapRow(entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val columns = entity.child.filter(x => x.label == "field")
      .map(f => (f.attribute("name").asInstanceOf[Some[Text]].get.data, f.attribute("type").asInstanceOf[Some[Text]].get.data))
      .map(f => ".set%s(rs.get%s(\"%s\"))".format(camelToPascal(f._2), camelToPascal(f._2), camelToPascal(f._1)))
      .reduce((x, y) => "%s\n      %s".format(x, y))

    val out =
      s"""new RowMapper<${entityName}Vo>() {
         |  public ${entityName}Vo mapRow(java.sql.ResultSet rs, int rowNum) {
         |    ${entityName}Vo row = ${entityName}Vo.newBuilder()
         |      ${columns}
         |      .build();
         |  }
         |}""".stripMargin
    out
  }


  private def paramMapper(entity: Node): String = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val columns = entity.child.filter(x => x.label == "field")
      .map(f => (f.attribute("name").asInstanceOf[Some[Text]].get.data, f.attribute("type").asInstanceOf[Some[Text]].get.data))
      .map(f => ".set%s(rs.get%s(\"%s\"))".format(camelToPascal(f._2), camelToPascal(f._2), camelToPascal(f._1)))
      .reduce((x, y) => "%s\n      %s".format(x, y))

    val out =
      s"""new QueryParamMapper() {
         |  public Object map(String s) {
         |  }
         |}""".stripMargin
    out
  }
}
