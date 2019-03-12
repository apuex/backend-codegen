package com.github.apuex.springbootsolution.runtime

import java.io.{BufferedReader, IOException, UncheckedIOException}
import java.util.NoSuchElementException


/**
  * This is a patch for scala line iterator, which is subject to change.
  *
  * Since the following approach is unstable because a bug exists in the
  * Scala standard library, we created this class to use java alternative
  * as walk-around.
  *
  * Source.fromFile("my-file.txt")
  *   .map(line -> ...)
  *   ...
  *
  * @param reader
  */
class ScalaLineIterator(reader: BufferedReader) extends Iterator[String] {
  var nextLine: String = null

  override def hasNext: Boolean = if (nextLine != null) return true
  else try {
    nextLine = reader.readLine
    if(nextLine == null) {
      reader.close()
      false
    } else true
  } catch {
    case e: IOException =>
      throw new UncheckedIOException(e)
  }

  override def next(): String = {
    if (nextLine != null || hasNext) {
      val line = nextLine
      nextLine = null
      return line
    }
    else throw new NoSuchElementException
  }
}
