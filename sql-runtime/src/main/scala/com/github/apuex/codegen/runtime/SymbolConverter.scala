package com.github.apuex.codegen.runtime

import com.github.apuex.codegen.runtime.SymbolConverter.Converter

trait SymbolConverter {
  def convert(s: String): String
  def converter: Converter = {
    case x: String => convert(x)
  }
}

object SymbolConverter {
  type Converter = PartialFunction[String, String]

  val identity: Converter = {
    case x: String => x
  }

  val camelToC: Converter = {
    case name: String => name.map(
      x => {
        if (x.isUpper) {
          s"_${x.toLower}"
        } else {
          x
        }
      }).foldLeft("")(_ + _)
  }

  val pascalToCamel: Converter = {
    case name: String =>
      if (name.length > 1)
        name.substring(0, 1).toLowerCase + name.substring(1)
      else
        name
  }

  val camelToPascal: Converter = {
    case name: String =>
      if (name.length > 1)
        name.substring(0, 1).toUpperCase + name.substring(1)
      else
        name
  }
}
