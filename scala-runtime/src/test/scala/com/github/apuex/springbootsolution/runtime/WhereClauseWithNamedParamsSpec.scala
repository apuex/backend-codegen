package com.github.apuex.springbootsolution.runtime

import com.github.apuex.springbootsolution.runtime.LogicalConnectionType.AND
import com.github.apuex.springbootsolution.runtime.PredicateType.EQ
import com.github.apuex.springbootsolution.runtime.QueryCommandMethods._
import org.scalatest._

class WhereClauseWithNamedParamsSpec extends FlatSpec with Matchers {
  "A WhereClauseWithNamedParams" should "generate single field filter predicate" in {
    val (predicate: FilterPredicate, params: Map[String, String]) = createPredicate(EQ, "name", "value")

    val q = QueryCommand(Some(predicate), params)

    // println(JsonFormat.printer().print(q))

    val whereClause = WhereClauseWithNamedParams(new CamelToPascalConverter())
    whereClause.toWhereClause(q) should be("WHERE Name = {name}")
  }

  it should "generate predicate with and" in {
    val criteria = Seq(
      createPredicate(EQ, "id", "value"),
      createPredicate(EQ, "name", "value")
    )
    val predicates = criteria
      .map(x => x._1)
    val params = criteria
      .flatMap(x => x._2)
      .toMap

    val connection = createConnection(AND, predicates)
    val q = QueryCommand(Some(connection), params)

    // println(JsonFormat.printer().print(q))

    val whereClause = WhereClauseWithNamedParams(new CamelToPascalConverter())
    whereClause.toWhereClause(q, 2) should be(
      """  WHERE Id = {id}
        |  AND Name = {name}""".stripMargin)
  }
}
