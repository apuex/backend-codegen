package com.github.apuex.springbootsolution.runtime

import com.google.gson.Gson
import org.scalatest.{FlatSpec, Matchers}

class GsonTestSpec extends FlatSpec with Matchers {
  val gson = new Gson()
  "A Gson" should "serialize Array[String to json" in {

    val stringArray = Array(
      "hello",
      "world"
    )

    val json = gson.toJson(stringArray)
    val jsonJson = gson.toJson(json)

    println(json)
    println(jsonJson)

    json should be (
      s"""
         |["hello","world"]
       """.stripMargin.trim)
    jsonJson should be (
      s"""
         |"[\\"hello\\",\\"world\\"]"
       """.stripMargin.trim)
  }

}
