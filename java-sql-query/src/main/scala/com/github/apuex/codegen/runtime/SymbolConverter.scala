package com.github.apuex.codegen.runtime

import com.github.apuex.codegen.runtime.SymbolConverters._

trait SymbolConverter {
  def convert(s: String): String
}

class CamelToCConverter extends SymbolConverter {
  override def convert(s: String): String = camelToC(s)
}

class IdentityConverter extends SymbolConverter {
  override def convert(s: String): String = s
}

class CamelToPascalConverter extends SymbolConverter {
  override def convert(s: String): String = camelToPascal(s)
}

class CamelToShellConverter extends SymbolConverter {
  override def convert(s: String): String = camelToPascal(s)
}

class PascalToCamelConverter extends SymbolConverter {
  override def convert(s: String): String = pascalToCamel(s)
}

class PascalToShellConverter extends SymbolConverter {
  override def convert(s: String): String = pascalToShell(s)
}

object SymbolConverters {
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

  val cToPascal: Converter = {
    case name: String => name.split("_").map(
      x => {
        x.substring(0, 1).toUpperCase() + x.substring(1)
    }).foldLeft("")(_ + _)
  }

  val cToCamel: Converter = {
    case name => pascalToCamel(cToPascal(name))
  }

  val cToShell: Converter = {
    case name => name.replace("_", "-")
  }

  val camelToShell: Converter = {
    case name: String => name.map(
      x => {
        if (x.isUpper) {
          s"-${x.toLower}"
        } else {
          x
        }
      }).foldLeft("")(_ + _)
  }

  val pascalToShell: Converter = {
    case name => camelToShell(pascalToCamel(name))
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
