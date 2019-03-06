package com.github.apuex.springbootsolution.codegen

import java.sql.{Connection, DriverManager}

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
    val conn = DriverManager.getConnection(url)
    val dbMeta = conn.getMetaData()
    val tables = dbMeta.getTables(null, user.toUpperCase(), null, Array("TABLE", "VIEW"))
    val namespace = System.getProperty("package", db)

    printf("<?xml version=\"1.0\"?>\n")
    printf("<model name=\"%s\" script=\"cqrs_entities.gsl\" package=\"%s\" dbSchema=\"%s\">\n", db, namespace, db)
    while (tables.next()) {
      val schema = tables.getString("TABLE_SCHEM").toLowerCase()
      val table = tables.getString("TABLE_NAME").toLowerCase()
      if(!table.contains("$")) {
        val keyRs = dbMeta.getPrimaryKeys(null, schema, table)
        var keys = Seq[String]()
        while(keyRs.next()) {
          keys :+= keyRs.getString("COLUMN_NAME")
        }
        dumpTableColumns(conn, schema, table, keys)
        keyRs.close()
      }
    }
    printf("</model>\n")
  }

  def dumpTableColumns(conn: Connection, schema: String, table: String, keys: Seq[String]) = {
    printf("  <!-- %s.%s -->\n", schema, table)
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery(String.format("SELECT * FROM %s.%s OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY", schema, table));
    val rsMeta = rs.getMetaData()

    printf("  <entity name=\"%s\" aggregationRoot=\"false\" enum=\"false\" generate=\"true\">\n", table)
    (1 to rsMeta.getColumnCount).foreach(i => {
      printf("    <field no=\"%s\" name=\"%s\" type=\"%s\" %srequired=\"%s\"/>\n",
        i,
        rsMeta.getColumnName(i),
        typeConverter(rsMeta.getColumnTypeName(i).toLowerCase()),
        lengthConverter(rsMeta.getColumnTypeName(i).toLowerCase(), rsMeta.getColumnDisplaySize(i)),
        if(rsMeta.isNullable(i) == 0) false else true
      )
    })
    if(!keys.isEmpty) {
      printf("    <primaryKey name=\"%s_pk\">\n", table)
      keys.foreach(field => printf("      <field name=\"%s\"/>\n", field))
      printf("    </primaryKey>\n")
    }
    printf("  </entity>\n")

    rs.close()
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
    case "real" => ""
    case "float" => ""
    case "double" => ""
    case "image" => ""
    case "blob" => ""
    case _ => ""
      //throw new IllegalArgumentException(x)
  }
}
