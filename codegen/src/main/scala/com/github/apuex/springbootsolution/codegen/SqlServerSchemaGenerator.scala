package com.github.apuex.springbootsolution.codegen

import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.xml.Node

object SqlServerSchemaGenerator extends App {
  val modelLoader = ModelLoader(args(0))
  new SqlServerSchemaGenerator(modelLoader).generate()
}

class SqlServerSchemaGenerator(modelLoader: ModelLoader) {

  import modelLoader._
  def generate(): Unit = {
    val daoMysqlResDir = s"${Dao.projectDir}/src/main/resources"
    save(s"${cToShell(modelDbSchema)}-db-sqlserver-schema.ddl",
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
         |-----------------------------------------------------
         |-- This file is 100% ***GENERATED***, DO NOT EDIT! --
         |-----------------------------------------------------
         |
         |-- Using '${modelDbSchema}' as database name, schema name, login name, user name and role name.
         |-- For Test/Demo purpose only.
         |-- Fine-tuning is need for production.
         |
         |DROP DATABASE IF EXISTS [${modelDbSchema}]
         |GO
         |
         |CREATE DATABASE [${modelDbSchema}] ON
         |  PRIMARY (
         |    NAME       = '${modelDbSchema}_data',
         |    FILENAME   = '/var/opt/mssql/data/${modelDbSchema}_data.mdf',
         |    SIZE       = 10MB,
         |    MAXSIZE    = UNLIMITED,
         |    FILEGROWTH = 10MB
         |  )
         |  LOG ON (
         |    NAME       = '${modelDbSchema}_log',
         |    FILENAME   = '/var/opt/mssql/data/${modelDbSchema}_log.ldf',
         |    SIZE       = 10MB,
         |    MAXSIZE    = UNLIMITED,
         |    FILEGROWTH = 10MB
         |  )
         |  COLLATE Chinese_PRC_CI_AS
         |GO
         |
         |-- The following options is executed on database '${modelDbSchema}'
         |
         |USE [${modelDbSchema}]
         |GO
         |
         |DROP SCHEMA IF EXISTS [${modelDbSchema}]
         |GO
         |
         |CREATE SCHEMA [${modelDbSchema}] AUTHORIZATION dbo
         |GO
         |
         |DROP USER IF EXISTS [${modelDbSchema}]
         |GO
         |
         |-- DROP LOGIN IF EXISTS [${modelDbSchema}]
         |DROP LOGIN [${modelDbSchema}]
         |GO
         |
         |-- Change it for production since password is sensitive data.
         |CREATE LOGIN [${modelDbSchema}] WITH PASSWORD='my-Secret-pw'
         |GO
         |
         |CREATE USER [${modelDbSchema}] FOR LOGIN [${modelDbSchema}] WITH DEFAULT_SCHEMA=[${modelDbSchema}]
         |GO
         |
         |DROP ROLE IF EXISTS [${modelName}]
         |GO
         |
         |CREATE ROLE [${modelName}] AUTHORIZATION dbo
         |GO
         |
         |ALTER ROLE [${modelName}] ADD MEMBER [${modelDbSchema}]
         |GO
         |
         |-- ALTER ROLE [${modelName}] DROP MEMBER [${modelDbSchema}]
         |-- GO
         |
         |GRANT SELECT, UPDATE, DELETE ON SCHEMA::[${modelDbSchema}] to [${modelDbSchema}]
         |
       """.stripMargin.trim
    )

    val tables = entities
      .map(x => {
        val keyFieldNames = x.primaryKey.fields
          .map(_.name)
          .toSet
        s"""
           |CREATE TABLE [${modelDbSchema}].[${x.name}] (
           |  ${indent(defTableFields(x.fields, if(x.primaryKey.generated) keyFieldNames else Set()), 2)}
           |)
           |GO
         """.stripMargin.trim
      })

    val primaryKeys = entities
      .map(x => {
        val keyFieldNames = x.primaryKey.fields
          .map(x => s"[${x.name}]")
          .reduceOption((l, r) => s"${l}, ${r}")
          .getOrElse("")
        if (x.primaryKey.generated)
          s"""
             |
         """.stripMargin.trim
        else
          s"""
             |ALTER TABLE [${modelDbSchema}].[${x.name}] WITH CHECK
             |ADD CONSTRAINT [${x.primaryKey.name}] PRIMARY KEY (${keyFieldNames})
             |GO
         """.stripMargin.trim
      })

    val foreignKeys = entities
      .map(x => {
        x.foreignKeys.map(k => {
          val keyFieldNames = k.fields
            .map(x => s"[${x.name}]")
            .reduceOption((l, r) => s"${l}, ${r}")
            .getOrElse("")
          val refFieldNames = k.fields
            .map(x => s"[${x.refField}]")
            .reduceOption((l, r) => s"${l}, ${r}")
            .getOrElse("")

          s"""
             |ALTER TABLE [${modelDbSchema}].[${x.name}] WITH CHECK
             |ADD CONSTRAINT [${k.name}] FOREIGN KEY (${keyFieldNames})
             |REFERENCES [${modelDbSchema}].[${k.refEntity}](${refFieldNames});
             |GO
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

  def defTableFields(fields: Seq[ModelLoader.Field], generatedKeys: Set[String]): String = {
    fields
      .filter(!_.transient)
      .filter(x => "array" != x._type && "map" != x._type)
      .map(x => {
        val fieldType = toMysqlType(x._type, x.length, x.scale)
        val nullOpt = if (x.required) "NOT NULL" else ""
        if(generatedKeys.contains(x.name))
          s"""
             |[${x.name}] ${fieldType} ${nullOpt} IDENTITY(1, 1) PRIMARY KEY
         """.stripMargin.trim
        else
          s"""
             |[${x.name}] ${fieldType} ${nullOpt}
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
    case "double" => "FLOAT"
    case "blob" => "BLOB"
    case _ => "INT" // enum type
  }
}
