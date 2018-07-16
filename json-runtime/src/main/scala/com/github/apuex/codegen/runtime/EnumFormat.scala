package com.github.apuex.codegen.runtime

import scalapb._
import play.api.libs.json._


object EnumFormat {
  def format[E<: GeneratedEnum](companion: GeneratedEnumCompanion[E]): Format[E] = new Format[E] {
    override def writes(o: E) = JsString(o.name)

    override def reads(json: JsValue) = {
      json match {
        case JsString(name) => JsSuccess(companion.fromName(name).get)
        case _ => JsError(json.toString())
      }
    }
  }
}
