package com.github.apuex.springbootsolution.runtime

import scalapb.{GeneratedEnum, GeneratedEnumCompanion}

object EnumConvert {
  def fromName[E<: GeneratedEnum](companion: GeneratedEnumCompanion[E], name: String): Option[E] = companion.fromName(name)
  def fromValue[E<: GeneratedEnum](companion: GeneratedEnumCompanion[E], value: Int): E = companion.fromValue(value)

  def toName[E<: GeneratedEnum](o: E): String = o.name
  def toValue[E<: GeneratedEnum](o: E): Int = o.value

  def toName[E<: GeneratedEnum](o: Option[E]): Option[String] = o.map(x => x.name)
  def toValue[E<: GeneratedEnum](o: Option[E]): Option[Int] = o.map(x => x.value)
}
