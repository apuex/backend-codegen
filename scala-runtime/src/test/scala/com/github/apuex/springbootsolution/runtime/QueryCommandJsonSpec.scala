package com.github.apuex.springbootsolution.runtime

import com.github.apuex.springbootsolution.runtime.FilterPredicate.Clause.{Connection, Predicate}
import com.github.apuex.springbootsolution.runtime.LogicalConnectionType.AND
import org.scalatest.{FlatSpec, Matchers}
import scalapb.json4s.JsonFormat.GenericCompanion
import scalapb.json4s.{Parser, Printer, TypeRegistry}

class QueryCommandJsonSpec extends FlatSpec with Matchers {
  val messagesCompanions = MessagesProto.messagesCompanions
  val registry: TypeRegistry = messagesCompanions
    .foldLeft(TypeRegistry())((r, mc) => r.addMessageByCompanion(mc.asInstanceOf[GenericCompanion]))
  val printer = new Printer().withTypeRegistry(registry)
  val parser = new Parser().withTypeRegistry(registry)

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

    println(printer.print(queryCommand))
    printer.print(queryCommand) should be (
      s"""
         |{"predicate":{"connection":{"predicates":[{"predicate":{"predicateType":"EQ","fieldName":"col1","paramNames":["col1"]}},{"predicate":{"predicateType":"EQ","fieldName":"col2","paramNames":["col2"]}}]}},"params":{"col1":"col1 value","col2":"col2 value"}}
       """.stripMargin.trim)
  }

}
