package com.github.apuex.springbootsolution.runtime

import com.github.apuex.springbootsolution.runtime.DateFormat._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.google.protobuf.util.Timestamps

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
    case "timestamp" => v => toDate(Timestamps.parse(v))
    case "float" => v => v.toFloat
    case "double" => v => v.toDouble
    case "blob" => v => v
    case _ =>
      v => cToPascal(v)
  }

  def toJdbcType(typeName: String): String = typeName match {
    case "bool" => "boolean"
    case "short" => "short"
    case "byte" => "byte"
    case "int" => "int"
    case "long" => "long"
    case "decimal" => "BigDecimal"
    case "string" => "String"
    case "timestamp" => "Timestamp"
    case "float" => "float"
    case "double" => "double"
    case "blob" => "Bytes"
    case _ => "int" // enum type
  }

  def toJavaType(typeName: String): String = typeName match {
    case "bool" => "boolean"
    case "short" => "short"
    case "byte" => "byte"
    case "int" => "int"
    case "long" => "long"
    case "decimal" => "BigDecimal"
    case "string" => "String"
    case "timestamp" => "Timestamp"
    case "float" => "float"
    case "double" => "double"
    case "blob" => "Bytes"
    case x =>
      cToPascal(x)
  }

  def toProtobufType(typeName: String): String = typeName match {
    case "bool" => typeName
    case "short" => "int32"
    case "byte" => "bytes"
    case "int" => "int32"
    case "identity" => "int32"
    case "long" => "int64"
    case "decimal" => "double"
    case "string" => typeName
    case "timestamp" => "google.protobuf.Timestamp"
    case "float" => typeName
    case "double" => typeName
    case "blob" => "bytes"
    case x =>
      cToPascal(x)
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
      pascalToC(x)
  }
}
