package com.github.apuex.springbootsolution.codegen

import _root_.scala.xml.parsing._
import scala.xml._

case class ModelLoader(fileName: String) {
  val factory = new NoBindingFactoryAdapter
  val xml: Node = factory.load(fileName)
}
