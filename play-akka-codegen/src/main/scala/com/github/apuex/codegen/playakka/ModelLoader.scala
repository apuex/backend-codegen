package com.github.apuex.codegen.playakka

import _root_.scala.xml.parsing._
import scala.xml._

case class ModelLoader(fileName: String) {
  val factory = new NoBindingFactoryAdapter
  val xml: Node = factory.load(fileName)
}
