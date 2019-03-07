package com.github.apuex.springbootsolution.codegen

import java.sql.{Connection, DriverManager, SQLSyntaxErrorException, DatabaseMetaData, ResultSet}

object DumpOracleSchema extends App {

  if(args.length < 4) {
    println("Usage:\n" +
      "\tjava -jar <this jar> <cmd> <host> <port> <db> <user> <password>")
  } else {
    dumpSchema(
      args(0),
      args(1),
      args(2),
      args(3),
      if(args.length == 4) "" else args(4)
    )
  }

  def dumpSchema(host: String, port: String, db: String, user: String, password: String) = {
    val url: String = String.format("jdbc:oracle:thin:%s/%s@//%s:%s/%s", user, password, host, port, db)
    val namespace = System.getProperty("package", db)
    val schema = System.getProperty("schema", db)
    val model = System.getProperty("model", schema)

    val conn = DriverManager.getConnection(url)
    val dbMeta = conn.getMetaData()
    val tables = dbMeta.getTables(null, schema.toUpperCase(), null, Array("TABLE"))
    val views = dbMeta.getTables(null, schema.toUpperCase(), null, Array("VIEW"))

    printf("<?xml version=\"1.0\"?>\n")
    printf("<model name=\"%s\" script=\"cqrs_entities.gsl\" package=\"%s\" dbSchema=\"%s\">\n", model, namespace, schema)
    printf("  <!-- ALL TABLES -->\n")
    dumpTable(conn, dbMeta, tables)
    tables.close()
    printf("  <!-- ALL VIEWS -->\n")
    dumpTable(conn, dbMeta, views)
    views.close()
    conn.close()
    printf("</model>\n")
  }

  private def dumpTable(conn: Connection, dbMeta: DatabaseMetaData, tables: ResultSet) = {
    while (tables.next()) {
      val schema = tables.getString("TABLE_SCHEM").toLowerCase()
      val table = tables.getString("TABLE_NAME").toLowerCase()
      if (!table.contains("$")) {
        val keyRs = dbMeta.getPrimaryKeys(null, schema.toUpperCase(), table.toUpperCase())
        var keys = Seq[String]()
        while (keyRs.next()) {
          keys :+= keyRs.getString("COLUMN_NAME").toLowerCase()
        }
        dumpTableColumns(conn, schema, table, keys)
        keyRs.close()
      }
    }
  }

  def dumpTableColumns(conn: Connection, schema: String, table: String, keys: Seq[String]) = {
    printf("  <!-- %s.%s -->\n", schema, table)
    val stmt = conn.createStatement()
    try {
      val sql =
        s"""
           |SELECT
           |    tc.owner,
           |    tc.table_name,
           |    tc.column_id,
           |    tc.column_name,
           |    tc.data_type,
           |    tc.data_length,
           |    tc.nullable,
           |    tc.data_default,
           |    cc.comments
           |FROM all_tab_columns tc
           |LEFT JOIN user_col_comments cc
           |ON tc.table_name = cc.table_name
           |    AND tc.column_name = cc.column_name
           |WHERE tc.owner='${schema.toUpperCase}'
           |    AND tc.table_name='${table.toUpperCase}'
           |ORDER BY tc.column_id ASC
         """.stripMargin
      val rs = stmt.executeQuery(sql);
      printf("  <entity name=\"%s\" aggregationRoot=\"false\" enum=\"false\" generate=\"true\">\n", table)
      while(rs.next()) {
        val fieldId = rs.getInt("column_id")
        val fieldName = rs.getString("column_name").toLowerCase()
        val dataType = rs.getString("data_type").toLowerCase()
        val fieldType = typeConverter(dataType)
        val fieldLength = rs.getInt("data_length")
        val fieldNullable = if(rs.getString("nullable").equalsIgnoreCase("Y")) false else true
        val fieldDefault = rs.getString("data_default")
        val fieldComments = rs.getString("comments")
        printf("    <field no=\"%s\" name=\"%s\" type=\"%s\" %srequired=\"%s\" %scomments=\"%s\"/>\n",
          fieldId,
          fieldName,
          fieldType,
          lengthConverter(dataType, fieldLength),
          fieldNullable,
          defaultConverter(fieldDefault),
          if(null == fieldComments) "" else fieldComments
        )
      }
      if (!keys.isEmpty) {
        printf("    <primaryKey name=\"%s_pk\">\n", table)
        keys.foreach(field => printf("      <field name=\"%s\"/>\n", field))
        printf("    </primaryKey>\n")
      }
      printf("  </entity>\n")

      rs.close()
    } catch {
      case e: SQLSyntaxErrorException => printf("<!-- %s -->", e.getMessage())
    }
    stmt.close()
  }

  private def typeConverter(name: String): String = name match {
    case "bit" => "bool"
    case "smallint" => "short"
    case "tinyint" => "byte"
    case "int" => "int"
    case "number" => "int"
    case "int identity" => "int"
    case "bigint" => "long"
    case "decimal" => "decimal"
    case "char" => "string"
    case "varchar" => "string"
    case "varchar2" => "string"
    case "nvarchar" => "string"
    case "ntext" => "string"
    case "text" => "string"
    case "datetime" => "timestamp"
    case "timestamp" => "timestamp"
    case "timestamp(6)" => "timestamp"
    case "real" => "float"
    case "float" => "double"
    case "double" => "double"
    case "image" => "blob"
    case "blob" => "blob"
    case x => x
      //throw new IllegalArgumentException(x)
  }

  private def lengthConverter(name: String, length: Int): String = name match {
    case "bit" => ""
    case "smallint" => ""
    case "tinyint" => ""
    case "int" => ""
    case "number" => ""
    case "int identity" => ""
    case "bigint" => ""
    case "decimal" => ""
    case "char" => "length=\"%d\" ".format(length)
    case "varchar" => "length=\"%d\" ".format(length)
    case "varchar2" => "length=\"%d\" ".format(length)
    case "nvarchar" => "length=\"%d\" ".format(length)
    case "ntext" => ""
    case "text" => ""
    case "datetime" => ""
    case "timestamp" => ""
    case "timestamp(6)" => ""
    case "real" => ""
    case "float" => ""
    case "double" => ""
    case "image" => ""
    case "blob" => ""
    case _ => ""
      //throw new IllegalArgumentException(x)
  }

  private def defaultConverter(str: String): String = {
    if(null == str) "" else {
      "default=\"%s\" ".format(
        str.trim()
          .stripPrefix("'")
          .stripSuffix("'")
          .trim()
      )
    }
  }

}
