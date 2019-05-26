package com.github.apuex.springbootsolution.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

import scala.xml.Text
import scala.xml.parsing._
import scala.xml._


object FilterEntity extends App {
  val xml = ModelLoader(args(0)).xml
  xml.child
  .filter(x => x.label == "entity")
  .filter(x => !(x.child.filter(c => c.label == "primaryKey").isEmpty))
  .foreach(println)
}
