package com.github.apuex.codegen.runtime

import java.util

import com.github.apuex.codegen.runtime.Messages._

object Message {
  def createConnection(connectionType: LogicalConnectionType, predicates: java.util.List[FilterPredicate]): FilterPredicate = {
    val connection = LogicalConnectionVo.newBuilder()
      .setLogicalConnectionType(connectionType)
      .addAllPredicates(predicates)
      .build()

    FilterPredicate.newBuilder()
      .setConnection(connection)
      .build()
  }

  def createPredicate(predicateType: PredicateType, name: String, value: String, params: util.Map[String, String]): FilterPredicate = {
    val paramNames = new util.ArrayList[String]
    paramNames.add(name)
    val predicateVo = LogicalPredicateVo.newBuilder()
      .setPredicateType(predicateType)
      .setFieldName(name)
      .addAllParamNames(paramNames)
      .build()

    val predicate = FilterPredicate.newBuilder()
      .setPredicate(predicateVo)
      .build()

    params.put(name, value)

    predicate
  }
}
