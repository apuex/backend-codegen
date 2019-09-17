package com.github.apuex.springbootsolution.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

import scala.xml._

object ApiList extends App {

  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("output.dir", "target/generated")}"
  val docsDir = s"${projectRoot}/${cToShell(modelName)}/docs"
  val hyphen = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-"

  new File(docsDir).mkdirs()

  val printWriter = new PrintWriter(s"${docsDir}/api-list.csv", "utf-8")

  xml.child.filter(x => x.label == "entity")
    .filter(x => x.attribute("aggregateRoot") == Some(Text("true")))
    .foreach(x => apiForEntity(modelPackage, x))

  printWriter.close()

  private def apiForEntity(modelPackage: String, entity: Node): Unit = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val format = "%s, %s\n"
    printWriter.print(format.format(s"${cToShell(entityName)}", s"${cToShell("%s%s%s".format("create", hyphen, cToShell(entityName)))}"))
    printWriter.print(format.format("", s"${cToShell("%s%s%s".format("retrieve", hyphen, cToShell(entityName)))}"))
    printWriter.print(format.format("", s"${cToShell("%s%s%s".format("update", hyphen, cToShell(entityName)))}"))
    printWriter.print(format.format("", s"${cToShell("%s%s%s".format("delete", hyphen, cToShell(entityName)))}"))
    printWriter.print(format.format("", s"${cToShell("%s%s%s".format("query", hyphen, cToShell(entityName)))}"))
  }

}
