package com.github.apuex.codegen.springboot

import java.io.{File, PrintWriter}

import com.github.apuex.codegen.runtime.SymbolConverters._
import com.github.apuex.codegen.runtime.TypeConverters._
import com.github.apuex.codegen.runtime.TextUtils._

import scala.xml.{Node, Text}

object Message extends App {
  val xml = ModelLoader(args(0)).xml
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val srcDir = s"dao/src/main/proto/${modelPackage.replace('.', '/')}/dao"

  new File(srcDir).mkdirs()
  val printWriter = new PrintWriter(s"${srcDir}/messages.proto", "utf-8")

  val prelude =
    s"""syntax = "proto3";
      |import "google/protobuf/timestamp.proto";
      |
      |package com.sample.message;
      |""".stripMargin

  printWriter.print(prelude)

  xml.child.filter(x => x.label == "entity")
    .foreach(x => {
      messageForEntity(modelPackage, x)
    })

  printWriter.close()

  def messageForEntity(modelPackage: String, entity: Node): Unit = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data

    val pkColumns = entity.child.filter(x => x.label == "primaryKey")
      .flatMap(k => k.child.filter(x => x.label == "field"))
      .map(f => f.attribute("name").asInstanceOf[Some[Text]].get.data)
      .toSet

    val columns = entity.child.filter(x => x.label == "field")
      .map(f => (
        f.attribute("no").asInstanceOf[Some[Text]].get.data,
        f.attribute("name").asInstanceOf[Some[Text]].get.data,
        f.attribute("type").asInstanceOf[Some[Text]].get.data
        )
      )

    val crud =
      s"""
        |message ${entityName}Vo {
        |${indent(fields(columns), 2)};
        |}
        |
        |message Create${entityName}Cmd {
        |${indent(fields(columns), 2)};
        |}
        |
        |message Update${entityName}Cmd {
        |${indent(fields(columns), 2)};
        |}
        |
        |message Delete${entityName}Cmd {
        |${indent(fields(columns.filter(f => pkColumns.contains(f._2))), 2)};
        |}
        |
        |message Retrieve${entityName}Cmd {
        |${indent(fields(columns.filter(f => pkColumns.contains(f._2))), 2)};
        |}
        |
      """.stripMargin

    printWriter.print(crud)
  }

  def fields(columns: Seq[(String, String, String)]): String = {
    columns.map(f => "%s %s = %s".format(toProtobufType(f._3), f._2, f._1))
      .reduce((x, y) => "%s;\n%s".format(x, y))
  }
}
