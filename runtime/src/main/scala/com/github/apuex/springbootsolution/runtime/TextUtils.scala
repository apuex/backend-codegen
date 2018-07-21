package com.github.apuex.springbootsolution.runtime

object TextUtils {
  def indent(lines: String, spaces: Int): String = {
    if(null == lines) null
    else {
      val indenting = s"${(0 until spaces).map(_ => " ").foldLeft("")(_ + _)}"
      lines.split("[\n|\r]")
        .map(l => "%s%s".format(indenting, l))
        .reduce((x, y) => "%s\n%s".format(x, y))
    }
  }
}
