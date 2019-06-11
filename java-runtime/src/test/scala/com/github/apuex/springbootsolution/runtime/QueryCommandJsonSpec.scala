package com.github.apuex.springbootsolution.runtime

import com.github.apuex.springbootsolution.runtime.LogicalConnectionType.AND
import com.github.apuex.springbootsolution.runtime.PredicateType.EQ
import com.google.protobuf.util.JsonFormat
import org.scalatest.{FlatSpec, Matchers}

class QueryCommandJsonSpec extends FlatSpec with Matchers {
  val registry = JsonFormat.TypeRegistry
    .newBuilder
    .add(Messages.getDescriptor.getMessageTypes)
    .build

  val printer = JsonFormat.printer().usingTypeRegistry(registry)

  val parser = JsonFormat.parser().usingTypeRegistry(registry)

  "A QueryCommand" should "serialize 'and eq' to json" in {
    val queryCommand = QueryCommand.newBuilder()
      .setPredicate(
        FilterPredicate.newBuilder()
          .setConnection(
            LogicalConnectionVo.newBuilder()
              .setLogicalConnectionType(AND)
              .addPredicates(
                FilterPredicate.newBuilder()
                  .setPredicate(
                    LogicalPredicateVo.newBuilder()
                      .setPredicateType(EQ)
                      .setFieldName("col1")
                      .addParamNames("col1")
                      .build()
                  )
                  .build()
              )
              .addPredicates(
                FilterPredicate.newBuilder()
                  .setPredicate(
                    LogicalPredicateVo.newBuilder()
                      .setPredicateType(EQ)
                      .setFieldName("col2")
                      .addParamNames("col2")
                      .build()
                  )
                  .build()
              )
              .build()
          )
          .build()
      )
      .putParams("col1", "col1 value")
      .putParams("col2", "col2 value")
      .build()

    // println(printer.print(queryCommand))
    val builder4j = QueryCommand.newBuilder()
    val builder4s = QueryCommand.newBuilder()
    val fromJava = parser.merge(
      s"""
         |{
         |  "predicate": {
         |    "connection": {
         |      "predicates": [{
         |        "predicate": {
         |          "predicateType": "EQ",
         |          "fieldName": "col1",
         |          "paramNames": ["col1"]
         |        }
         |      }, {
         |        "predicate": {
         |          "predicateType": "EQ",
         |          "fieldName": "col2",
         |          "paramNames": ["col2"]
         |        }
         |      }]
         |    }
         |  },
         |  "params": {
         |    "col1": "col1 value",
         |    "col2": "col2 value"
         |  }
         |}
       """.stripMargin.trim,
      builder4j)
    val fromScala = parser.merge(
      s"""
         |{
         |  "predicate" : {
         |    "connection" : {
         |      "predicates" : [ {
         |        "predicate" : {
         |          "predicateType" : "EQ",
         |          "fieldName" : "col1",
         |          "paramNames" : [ "col1" ]
         |        }
         |      }, {
         |        "predicate" : {
         |          "predicateType" : "EQ",
         |          "fieldName" : "col2",
         |          "paramNames" : [ "col2" ]
         |        }
         |      } ]
         |    }
         |  },
         |  "params" : {
         |    "col1" : "col1 value",
         |    "col2" : "col2 value"
         |  }
         |}
       """.stripMargin.trim,
      builder4s
    )
    val expected = printer.print(queryCommand)
    printer.print(builder4j.build()) should be(expected)
    printer.print(builder4s.build()) should be(expected)

  }

}
