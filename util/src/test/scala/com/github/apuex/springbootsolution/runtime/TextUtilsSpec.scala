package com.github.apuex.springbootsolution.runtime
import com.github.apuex.springbootsolution.runtime.TextUtils._

import org.scalatest._

class TextUtilsSpec extends FlatSpec with Matchers{
  "A indent function" should "indent lines" in {
    val lines =
      s"""
         |hello,
         |world!
       """.stripMargin.trim

    val indentSkipFirst = indent(lines, 2)
    val indentFirst = indent(lines, 2, true)

    indentSkipFirst should be(
      s"""hello,
         |  world!""".stripMargin
    )
    indentFirst should be(
      s"""  hello,
         |  world!""".stripMargin
    )
  }

  "A indentWithRightMargin function" should "indent lines" in {
    val lines =
      s"""
         |hello,
         |world!
       """.stripMargin.trim

    val indentSkipFirst = indentWithRightMargin(lines, 2)
    val indentFirst = indentWithRightMargin(lines, 2, true)

    indentSkipFirst should be(
      s"""hello,
         |  |world!""".stripMargin
    )
    indentFirst should be(
      s"""|  |hello,
         |  |world!""".stripMargin
    )
  }

  "A indentWithLeftMargin function" should "indent lines" in {
    val lines =
      s"""
         |hello,
         |world!
       """.stripMargin.trim

    val indentSkipFirst = indentWithLeftMargin(lines, 2)
    val indentFirst = indentWithLeftMargin(lines, 2, true)

    indentSkipFirst should be(
      s"""|hello,
          ||  world!""".stripMargin
    )
    indentFirst should be(
      s"""||  hello,
          ||  world!""".stripMargin
    )
  }

  "A indentWithLeftMarginForBlockQuote function" should "indent lines" in {
    val lines =
      s"""
         |hello,
         |world!
       """.stripMargin.trim

    val indentSkipFirst = indentWithLeftMarginForQuote(lines, 2)
    val indentFirst = indentWithLeftMarginForQuote(lines, 2, true)

    indentSkipFirst should be(
      s"""||hello,
          ||  |world!""".stripMargin
    )
    indentFirst should be(
      s"""||  |hello,
          ||  |world!""".stripMargin
    )
  }

  "A blockQuote function" should "block quote lines" in {
    val lines =
      s"""
         |hello,
         |world!
       """.stripMargin.trim

    val indentSkipFirst = blockQuote(lines, 2)
    val indentFirst = blockQuote(lines, 2, true)

    indentSkipFirst should be(
      "s\"\"\"\n   |hello,\n   |  world!\n \"\"\".stripMargin.trim"
    )
    indentFirst should be(
      "s\"\"\"\n   |  hello,\n   |  world!\n \"\"\".stripMargin.trim"
    )
  }
}
