package com.github.apuex.codegen.runtime

import java.util

import com.github.apuex.codegen.runtime.Message._
import com.github.apuex.codegen.runtime.Messages.LogicalConnectionType._
import com.github.apuex.codegen.runtime.Messages.PredicateType._
import com.github.apuex.codegen.runtime.Messages._
import com.github.apuex.codegen.runtime.SymbolConverters._

import scala.collection.JavaConverters._
import org.scalatest._

class WhereClauseWithUnnamedParamsSpec extends FlatSpec with Matchers {
  "A WhereClauseWithUnnamedParams" should "generate single field filter predicate" in {
    val params = new util.HashMap[String, String]()
    val predicate: FilterPredicate = createPredicate(EQ, "name", "value", params)

    val q = QueryCommand.newBuilder()
      .setPredicate(predicate)
      .putAllParams(params)
      .build()

    val whereClause = WhereClauseWithUnnamedParams(camelToPascal)
    whereClause.toWhereClause(q) should be("WHERE Name = ?")
    val expected = new util.ArrayList[String]()
    expected.add("value")
    whereClause.toUnnamedParamList(q) should be(expected)
  }

  it should "generate predicate with and" in {
    val predicates = new util.ArrayList[FilterPredicate]()
    val params = new util.HashMap[String, String]()
    predicates.add(createPredicate(EQ, "id", "id_value", params))
    predicates.add(createPredicate(EQ, "name", "name_value", params))
    val connection = createConnection(AND, predicates)

    val q = QueryCommand.newBuilder()
      .setPredicate(connection)
      .putAllParams(params)
      .build()

    val whereClause = WhereClauseWithUnnamedParams(camelToPascal)
    println(whereClause.toWhereClause(q, 2))
    whereClause.toWhereClause(q, 2) should be(
      """  WHERE (Id = ?
        |  AND Name = ?)""".stripMargin)
    val expected = new util.ArrayList[String]()
    expected.add("id_value")
    expected.add("name_value")
    whereClause.toUnnamedParamList(q) should be(expected)
  }
}
