package com.github.apuex.springbootsolution.codegen

import com.github.apuex.springbootsolution.runtime.SymbolConverters.cToShell

import _root_.scala.xml.parsing._
import scala.xml._

object ModelLoader {
  def apply(fileName: String): ModelLoader = {
    val factory = new NoBindingFactoryAdapter
    new ModelLoader(factory.load(fileName))
  }

  def apply(xml: Node): ModelLoader = new ModelLoader(xml)
}

class ModelLoader(val xml: Node) {
  val modelName = xml.\@("name")
  val modelPackage = xml.\@("package")
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  def projectDir(name: String) = s"${projectRoot}/${cToShell(modelName)}/${cToShell(modelName)}-${name}"
  def srcDir(name: String) = s"${projectDir(name)}/src/main/java/${modelPackage.replace('.', '/')}/${name}"
  val symboConverter = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}")
    "new IdentityConverter()" else "new CamelToCConverter()"
  val docsDir = s"${projectRoot}/${cToShell(modelName)}/docs"
  val hyphen = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-"
}
