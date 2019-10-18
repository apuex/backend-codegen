package com.github.apuex.springbootsolution.runtime

import java.util.{Date, UUID}

import com.google.protobuf.timestamp.Timestamp
import scalapb.{GeneratedEnum, GeneratedEnumCompanion}


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
    override def parse(s: String): Timestamp = DateFormat.parseScalapbTimestamp(s)
  }

  case object UUIDParser extends Parser[UUID] {
    override def parse(s: String): UUID = UUID.fromString(s)
  }

  case object DateParser extends Parser[Date] {
    override def parse(s: String): Date = DateFormat.parseTimestamp(s)
  }

  case class EnumParser[E<: GeneratedEnum](companion: GeneratedEnumCompanion[E]) extends Parser[E] {
    override def parse(s: String): E = if(isDigit(s)) companion.fromValue(s.toInt) else companion.fromName(s).get
  }

  def isDigit(s: String): Boolean = s forall Character.isDigit
}

sealed trait Parser[T] {
  def parse(s: String): T
}


