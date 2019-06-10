package com.github.apuex.springbootsolution.runtime

trait QueryParamMapper {
  def map(name: String, value: String): Object
  def exists(name: String): Boolean
}

