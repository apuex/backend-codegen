package com.github.apuex.springbootsolution.runtime

import java.util

import com.google.gson.Gson
import org.scalatest.{FlatSpec, Matchers}

class JsonStringArrayParserSpec  extends FlatSpec with Matchers {
  "A Json string array parser" should "parse json to array to java array" in {
    val gson = new Gson()
    val jsonStringArray = s"""["hello", "world"]"""
    val result = gson.fromJson(jsonStringArray, classOf[Array[String]])

    result should be (Array[String]("hello", "world"))
  }
}
