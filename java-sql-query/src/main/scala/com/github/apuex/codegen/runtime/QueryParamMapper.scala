package com.github.apuex.codegen.runtime

trait QueryParamMapper {
  def map(name: String, value: String): Object
}

