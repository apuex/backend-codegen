package com.github.apuex.codegen.runtime

import java.util

import com.github.apuex.codegen.runtime.Message._
import com.github.apuex.codegen.runtime.SymbolConverter._
import com.github.apuex.codegen.runtime.Messages._
import com.github.apuex.codegen.runtime.Messages.PredicateType._
import com.github.apuex.codegen.runtime.Messages.LogicalConnectionType._
import org.scalatest._

class WhereClauseWithNamedParamsSpec extends FlatSpec with Matchers {
  "A WhereClauseWithNamedParams" should "generate single field filter predicate" in {
    val predicate: FilterPredicate = createPredicate(EQ, "name", "value")

    val q = QueryCommand.newBuilder()
      .setPredicate(predicate)
      .build()

    val whereClause = WhereClauseWithNamedParams(camelToPascal)
    whereClause.toWhereClause(q) should be("WHERE Name = {name}")
  }

  "A WhereClauseWithNamedParams" should "generate predicate with and" in {
    val predicates = new util.ArrayList[FilterPredicate]()
    predicates.add(createPredicate(EQ, "id", "value"))
    predicates.add(createPredicate(EQ, "name", "value"))
    val connection = createConnection(AND, predicates)
    val q = QueryCommand.newBuilder()
      .setPredicate(connection)
      .build()

    val whereClause = WhereClauseWithNamedParams(camelToPascal)
    println(whereClause.toWhereClause(q, 2))
    whereClause.toWhereClause(q, 2) should be(
      """  WHERE (Id = {id}
        |  AND Name = {name})""".stripMargin)
  }
}
