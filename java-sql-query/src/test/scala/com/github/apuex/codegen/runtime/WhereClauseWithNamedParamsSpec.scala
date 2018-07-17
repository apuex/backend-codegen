package com.github.apuex.codegen.runtime

import java.util

import com.github.apuex.codegen.runtime.Message._
import com.github.apuex.codegen.runtime.SymbolConverters._
import com.github.apuex.codegen.runtime.Messages._
import com.github.apuex.codegen.runtime.Messages.PredicateType._
import com.github.apuex.codegen.runtime.Messages.LogicalConnectionType._
import org.scalatest._

class WhereClauseWithNamedParamsSpec extends FlatSpec with Matchers {
  "A WhereClauseWithNamedParams" should "generate single field filter predicate" in {
    val params = new util.HashMap[String, String]()
    val predicate: FilterPredicate = createPredicate(EQ, "name", "value", params)

    val q = QueryCommand.newBuilder()
      .setPredicate(predicate)
      .build()

    val whereClause = WhereClauseWithNamedParams(camelToPascal)
    whereClause.toWhereClause(q) should be("WHERE Name = {name}")
  }

  it should "generate predicate with and" in {
    val predicates = new util.ArrayList[FilterPredicate]()
    val params = new util.HashMap[String, String]()
    predicates.add(createPredicate(EQ, "id", "value", params))
    predicates.add(createPredicate(EQ, "name", "value", params))
    val connection = createConnection(AND, predicates)
    val q = QueryCommand.newBuilder()
      .setPredicate(connection)
      .build()

    val whereClause = WhereClauseWithNamedParams(camelToPascal)
    whereClause.toWhereClause(q, 2) should be(
      """  WHERE (Id = {id}
        |  AND Name = {name})""".stripMargin)
  }
}
