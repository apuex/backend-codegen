package com.github.apuex.springbootsolution.codegen

import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.xml.Node

object MysqlSchemaGenerator extends App {
  val modelLoader = ModelLoader(args(0))
  new MysqlSchemaGenerator(modelLoader).generate()
}
class MysqlSchemaGenerator(modelLoader: ModelLoader) {

  import modelLoader._
  def generate(): Unit = {
    val daoMysqlResDir = s"${Dao.projectDir}/src/main/resources"
    save(s"${cToShell(modelDbSchema)}-db-mysql-schema.ddl",
      generateDaoContent(xml),
      daoMysqlResDir)
  }

  def generateDaoContent(xml: Node): String = {
    val entities = xml.child
      .filter(_.label == "entity")
      .filter(x => "true" != x.\@("transient"))
      .sortWith((x, y) => depends(x, y))
      .map(x => {
        val name = x.\@("name")
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false
        if (!enum && "" == aggregatesTo)
          toValueObject(x, name, xml)
        else
          toValueObject(x, aggregatesTo, xml)
      })

    val prelude = Seq(
      s"""
         |/*****************************************************
         | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
         | *****************************************************/
         |
         |DROP USER IF EXISTS '${modelDbSchema}';
         |DROP DATABASE IF EXISTS ${modelDbSchema};
         |
         |CREATE DATABASE ${modelDbSchema} DEFAULT CHARACTER SET 'UTF8' DEFAULT COLLATE utf8_unicode_ci;
         |
         |CREATE USER '${modelDbSchema}' IDENTIFIED BY 'password';
         |GRANT ALL PRIVILEGES ON *.* TO '${modelDbSchema}' WITH GRANT OPTION;
         |
         |USE ${modelDbSchema};
       """.stripMargin.trim
    )

    val tables = entities
      .map(x => {
        s"""
           |CREATE TABLE ${modelDbSchema}.${x.name} (
           |  ${indent(defTableFields(x.fields), 2)}
           |) ENGINE=InnoDB DEFAULT CHARSET=utf8;
         """.stripMargin.trim
      })

    val primaryKeys = entities
      .map(x => {
        val keyFieldNames = x.primaryKey.fields
          .map(_.name)
          .reduceOption((l, r) => s"${l}, ${r}")
          .getOrElse("")
        if (x.primaryKey.generated)
          s"""
             |ALTER TABLE ${modelDbSchema}.${x.name} ADD INDEX ${x.primaryKey.name}(${keyFieldNames}), MODIFY ${keyFieldNames} BIGINT NOT NULL AUTO_INCREMENT;
         """.stripMargin.trim
        else
          s"""
             |ALTER TABLE ${modelDbSchema}.${x.name} ADD CONSTRAINT ${x.primaryKey.name} PRIMARY KEY(${keyFieldNames});
         """.stripMargin.trim
      })

    val foreignKeys = entities
      .map(x => {
        x.foreignKeys.map(k => {
          val keyFieldNames = k.fields
            .map(_.name)
            .reduceOption((l, r) => s"${l}, ${r}")
            .getOrElse("")
          val refFieldNames = k.fields
            .map(_.refField)
            .reduceOption((l, r) => s"${l}, ${r}")
            .getOrElse("")

          s"""
             |ALTER TABLE ${modelDbSchema}.${x.name} ADD CONSTRAINT ${k.name} FOREIGN KEY(${keyFieldNames}) REFERENCES ${modelDbSchema}.${k.refEntity}(${refFieldNames});
         """.stripMargin.trim
        })
      })
      .flatMap(x => x)

    (prelude ++
      tables ++
      primaryKeys ++
      foreignKeys)
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def defTableFields(fields: Seq[ModelLoader.Field]): String = {
    fields
      .filter(!_.transient)
      .filter(x => "array" != x._type && "map" != x._type)
      .map(x => {
        val fieldType = toMysqlType(x._type, x.length, x.scale)
        val nullOpt = if (x.required) "NOT NULL" else ""
        s"""
           |${x.name} ${fieldType} ${nullOpt}
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")
  }

  def toMysqlType(typeName: String, length: Int, scale: Int): String = typeName match {
    case "bool" => "TINYINT"
    case "short" => "SHORT"
    case "byte" => if (length == 0) "CHAR" else if (length > 0 && length < 256) s"CHAR(${length})" else "BLOB"
    case "int" => "INT"
    case "long" => "BIGINT"
    case "decimal" => s"DECIMAL(${length}, ${scale})"
    case "string" => if (length < 256) s"VARCHAR(${length})" else "TEXT"
    case "text" => "TEXT"
    case "timestamp" => "DATETIME"
    case "float" => "FLOAT"
    case "double" => "DOUBLE"
    case "blob" => "BLOB"
    case _ => "INT" // enum type
  }
}
