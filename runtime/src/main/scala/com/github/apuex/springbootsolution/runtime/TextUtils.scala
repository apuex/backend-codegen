package com.github.apuex.springbootsolution.runtime

object TextUtils {
  def indent(lines: String, spaces: Int, indentFirstLine: Boolean = false): String = {
    if(null == lines) null
    else {
      val indenting = s"${(0 until spaces).map(_ => " ").foldLeft("")(_ + _)}"
      if(indentFirstLine) {
        lines.split("[\n|\r]")
          .map(l => "%s%s".format(indenting, l))
          .reduce((x, y) => "%s\n%s".format(x, y))
      } else {
        lines.split("[\n|\r]")
          .reduce((x, y) => "%s\n%s%s".format(x, indenting, y))
      }
    }
  }
}
