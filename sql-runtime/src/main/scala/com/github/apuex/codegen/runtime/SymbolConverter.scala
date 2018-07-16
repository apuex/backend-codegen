package com.github.apuex.codegen.runtime

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
