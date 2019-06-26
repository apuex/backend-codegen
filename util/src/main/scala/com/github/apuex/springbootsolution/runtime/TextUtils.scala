package com.github.apuex.springbootsolution.runtime

object TextUtils {
  def indent(lines: String, spaces: Int, indentFirstLine: Boolean = false): String = {
    if(null == lines) null
    else {
      val indenting = s"${(0 until spaces).map(_ => " ").foldLeft("")(_ + _)}"
      val indented = lines.split("[\n\r]")
        .map(l => "%s%s".format(indenting, l))
        .map(x => if("" == x.trim) "" else x)
        .reduceOption((x, y) => "%s\n%s".format(x, y))
        .getOrElse("")
      if(indentFirstLine) {
        indented
      } else {
        if(indented.length > spaces) indented.substring(spaces)
        else indented
      }
    }
  }

  def indentWithRightMargin(lines: String, spaces: Int, indentFirstLine: Boolean = false): String = {
    if(null == lines) null
    else {
      val indenting = s"${(0 until spaces).map(_ => " ").foldLeft("")(_ + _)}|"
      val indented = lines.split("[\n\r]")
        .map(l => "%s%s".format(indenting, l))
        .map(x => if("" == x.trim) "" else x)
        .reduceOption((x, y) => "%s\n%s".format(x, y))
        .getOrElse("")
      if(indentFirstLine) {
        indented
      } else {
        if(indented.length > spaces + 1) indented.substring(spaces + 1)
        else indented
      }
    }
  }
  
  def indentWithLeftMargin(lines: String, spaces: Int, indentFirstLine: Boolean = false): String = {
    if(null == lines) null
    else {
      val indenting = s"|${(0 until spaces).map(_ => " ").foldLeft("")(_ + _)}"
      val indented = lines.split("[\n\r]")
        .map(l => "%s%s".format(indenting, l))
        .map(x => if("" == x.trim) "" else x)
        .reduceOption((x, y) => "%s\n%s".format(x, y))
        .getOrElse("")
      if(indentFirstLine) {
        indented
      } else {
        if(indented.length > spaces + 1) indented.substring(spaces + 1)
        else indented
      }
    }
  }

  def indentWithLeftMarginForQuote(lines: String, spaces: Int, indentFirstLine: Boolean = false): String = {
    if(null == lines) null
    else {
      val indenting = s"|${(0 until spaces).map(_ => " ").foldLeft("")(_ + _)}|"
      val indented = lines.split("[\n\r]")
        .map(l => "%s%s".format(indenting, l))
        .map(x => if("" == x.trim) "" else x)
        .reduceOption((x, y) => "%s\n%s".format(x, y))
        .getOrElse("")
      if(indentFirstLine) {
        indented
      } else {
        if(indented.length > spaces + 1) indented.substring(spaces + 1)
        else indented
      }
    }
  }
  
  def blockQuote(lines: String, spaces: Int, indentFirstLine: Boolean = false): String = {
    s"""
       |s\"\"\"
       |   ${indentWithLeftMarginForQuote(indent(lines, spaces, indentFirstLine), 3)}
       | \"\"\".stripMargin.trim
     """.stripMargin.trim
  }
}
