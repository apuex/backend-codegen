package com.github.apuex.codegen.runtime

import com.github.apuex.codegen.runtime.DateFormat._

trait TypeConverter {
  def convert(value: String): Any
}

object TypeConverters {
  def toJavaTypeConverter(typeName: String): TypeConverter = typeName match {
    case "bool" => v => v.toBoolean
    case "short" => v => v.toShort
    case "byte" => v => v.toByte
    case "int" => v => v.toInt
    case "long" => v => v.toLong
    case "decimal" => v => BigDecimal.apply(v)
    case "string" => v => v
    case "timestamp" => v => parseTimestamp(v)
    case "float" => v => v.toFloat
    case "double" => v => v.toDouble
    case "image" => v => v
    case x =>
      throw new IllegalArgumentException(x)
  }

  def toProtobufType(typeName: String): String = typeName match {
    case "bool" => typeName
    case "short" => "int32"
    case "byte" => "bytes"
    case "int" => "int32"
    case "long" => "int64"
    case "decimal" => "double"
    case "string" => typeName
    case "timestamp" => "google.protobuf.Timestamp"
    case "float" => typeName
    case "double" => typeName
    case "image" => "bytes"
    case x =>
      throw new IllegalArgumentException(x)
  }

  def toModelTypeName(typeName: String): String = typeName match {
    case "bit" => "bool"
    case "smallint" => "short"
    case "tinyint" => "byte"
    case "int" => "int"
    case "int identity" => "int"
    case "bigint" => "long"
    case "decimal" => "decimal"
    case "varchar" => "string"
    case "nvarchar" => "string"
    case "text" => "string"
    case "datetime" => "timestamp"
    case "real" => "float"
    case "float" => "double"
    case "double" => "double"
    case "image" => "blob"
    case x =>
      throw new IllegalArgumentException(x)
  }
}
