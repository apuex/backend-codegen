package com.github.apuex.springbootsolution.runtime

import com.github.apuex.springbootsolution.runtime.FilterPredicate.Clause.{Connection, Predicate}
import com.github.apuex.springbootsolution.runtime.LogicalConnectionType.AND
import com.github.apuex.springbootsolution.runtime.OrderType._
import com.google.gson.Gson
import org.scalatest.{FlatSpec, Matchers}
import scalapb.json4s.JsonFormat.GenericCompanion
import scalapb.json4s._
import org.json4s.jackson.JsonMethods._

class QueryCommandJsonSpec extends FlatSpec with Matchers {
  val messagesCompanions = MessagesProto.messagesCompanions
  val registry: TypeRegistry = messagesCompanions
    .foldLeft(TypeRegistry())((r, mc) => r.addMessageByCompanion(mc.asInstanceOf[GenericCompanion]))
  val printer = new Printer().withTypeRegistry(registry)
  val parser = new scalapb.json4s.Parser().withTypeRegistry(registry)

  "A QueryCommand" should "serialize no filter predicate to json" in {
    val queryCommand = QueryCommand()

    println(pretty(printer.toJson(queryCommand)))
    pretty(printer.toJson(queryCommand)) should be (
      s"""
         |{ }
       """.stripMargin.trim)
  }

  "A QueryCommand" should "serialize 'and eq' to json" in {
    val queryCommand = QueryCommand(
      Some(
        FilterPredicate(
          Connection(
            LogicalConnectionVo(
              AND,
              Seq(
                FilterPredicate(
                  Predicate(
                    LogicalPredicateVo(
                      PredicateType.EQ,
                      "col1",
                      Seq("col1")
                    )
                  )
                ),
                FilterPredicate(
                  Predicate(
                    LogicalPredicateVo(
                      PredicateType.EQ,
                      "col2",
                      Seq("col2")
                    )
                  )
                ),
              )
            )
          )
        )
      ),
      Map(
        "col1" -> "col1 value",
        "col2" -> "col2 value"
      )
    )

    println(pretty(printer.toJson(queryCommand)))
    pretty(printer.toJson(queryCommand)) should be (
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
       """.stripMargin.trim)
  }

  it should "serialize query command with pagination" in {
    val queryCommand = QueryCommand(
      Some(
        FilterPredicate(
          Connection(
            LogicalConnectionVo(
              AND,
              Seq(
                FilterPredicate(
                  Predicate(
                    LogicalPredicateVo(
                      PredicateType.EQ,
                      "name",
                      Seq("name")
                    )
                  )
                ),
              )
            )
          )
        )
      ),
      Map(
        "name" -> "user"
      ),
      1,
      10,
      Seq(
        OrderBy("name", ASC)
      )
    )

    println(pretty(printer.toJson(queryCommand)))
    pretty(printer.toJson(queryCommand)) should be (
    s"""
       |{
       |  "predicate" : {
       |    "connection" : {
       |      "predicates" : [ {
       |        "predicate" : {
       |          "predicateType" : "EQ",
       |          "fieldName" : "name",
       |          "paramNames" : [ "name" ]
       |        }
       |      } ]
       |    }
       |  },
       |  "params" : {
       |    "name" : "user"
       |  },
       |  "pageNumber" : 1,
       |  "rowsPerPage" : 10,
       |  "orderBy" : [ {
       |    "fieldName" : "name"
       |  } ]
       |}
     """.stripMargin.trim)
  }

  it should "serialize query command with 'in' predicate and pagination" in {
    val gson = new Gson()

    val queryCommand = QueryCommand(
      Some(
        FilterPredicate(
          Connection(
            LogicalConnectionVo(
              AND,
              Seq(
                FilterPredicate(
                  Predicate(
                    LogicalPredicateVo(
                      PredicateType.IN,
                      "name",
                      Seq("name")
                    )
                  )
                ),
              )
            )
          )
        )
      ),
      Map(
        "name" -> gson.toJson(Array("bill", "gates", "steve", "jobs"))
      ),
      1,
      10,
      Seq(
        OrderBy("name", ASC)
      )
    )

    println(pretty(printer.toJson(queryCommand)))
    pretty(printer.toJson(queryCommand)) should be (
      s"""
         |{
         |  "predicate" : {
         |    "connection" : {
         |      "predicates" : [ {
         |        "predicate" : {
         |          "predicateType" : "IN",
         |          "fieldName" : "name",
         |          "paramNames" : [ "name" ]
         |        }
         |      } ]
         |    }
         |  },
         |  "params" : {
         |    "name" : "[\\"bill\\",\\"gates\\",\\"steve\\",\\"jobs\\"]"
         |  },
         |  "pageNumber" : 1,
         |  "rowsPerPage" : 10,
         |  "orderBy" : [ {
         |    "fieldName" : "name"
         |  } ]
         |}
     """.stripMargin.trim)
  }

}
