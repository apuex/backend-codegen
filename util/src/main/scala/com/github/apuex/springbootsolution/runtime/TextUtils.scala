package com.github.apuex.springbootsolution.runtime

object TextUtils {
  def indent(lines: String, spaces: Int, indentFirstLine: Boolean = false): String = {
    if(null == lines) null
    else {
      val indenting = s"${(0 until spaces).map(_ => " ").foldLeft("")(_ + _)}"
      val indented = lines.split("[\n|\r]")
        .map(l => "%s%s".format(indenting, l))
        .map(x => if("" == x.trim) "" else x)
        .reduceOption((x, y) => "%s\n%s".format(x, y))
        .getOrElse("")
      if(indentFirstLine) {
        indented
      } else {
        indented.substring(spaces)
      }
    }
  }
}
