package com.github.apuex.codegen.runtime

import java.util.Date

import com.google.protobuf.timestamp.Timestamp

import scalapb._


object Parser {

  case object BooleanParser extends Parser[Boolean] {
    override def parse(s: String): Boolean = java.lang.Boolean.valueOf(s)
  }

  case object IntParser extends Parser[Int] {
    override def parse(s: String): Int = java.lang.Integer.valueOf(s)
  }

  case object LongParser extends Parser[Long] {
    override def parse(s: String): Long = java.lang.Long.valueOf(s)
  }

  case object DoubleParser extends Parser[Double] {
    override def parse(s: String): Double = java.lang.Double.valueOf(s)
  }

  case object StringParser extends Parser[String] {
    override def parse(s: String): String = s
  }

  case object TimestampParser extends Parser[Timestamp] {
    override def parse(s: String): Timestamp = DateFormat.parseProtobufTimestamp(s)
  }

  case object DateParser extends Parser[Date] {
    override def parse(s: String): Date = DateFormat.parseTimestamp(s)
  }

  case class EnumParser[E<: GeneratedEnum](companion: GeneratedEnumCompanion[E]) extends Parser[E] {
    override def parse(s: String): E = companion.fromName(s).get
  }
}

sealed trait Parser[T] {
  def parse(s: String): T
}


