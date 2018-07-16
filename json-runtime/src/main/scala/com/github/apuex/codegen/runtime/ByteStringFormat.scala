package com.github.apuex.codegen.runtime

import com.google.protobuf.ByteString
import play.api.libs.json._

object ByteStringFormat {
  def format: Format[ByteString] = new Format[ByteString] {
    override def writes(o: ByteString) = JsArray(o.toByteArray.toIndexedSeq.map(x => JsNumber(BigDecimal.apply(x.intValue()))))

    override def reads(json: JsValue) = {
      json match {
        case JsArray(value: Seq[JsValue]) =>
          JsSuccess(ByteString.copyFrom(value.toArray.flatMap(toByte)))
        case _ => JsError(json.toString())
      }
    }
  }

  def toByte(value: JsValue): Option[Byte] = {
    try {
      value match {
        case JsNumber(x) => Some(x.toByte)
        case JsString(x) => Some(x.toByte)
        case _ => None
      }
    } catch {
      case _: Exception => None
    }
  }
}
