package com.github.apuex.springbootsolution.runtime

import java.util

import com.github.apuex.springbootsolution.runtime.LogicalConnectionType._
import com.github.apuex.springbootsolution.runtime.Message._
import com.github.apuex.springbootsolution.runtime.PredicateType._
import com.google.protobuf.util.JsonFormat
import org.scalatest._

class WhereClauseWithUnnamedParamsSpec extends FlatSpec with Matchers {
  "A WhereClauseWithUnnamedParams" should "generate single field filter predicate" in {
    val params = new util.HashMap[String, String]()
    val predicate: FilterPredicate = createPredicate(EQ, "name", "value", params)

    val q = QueryCommand.newBuilder()
      .setPredicate(predicate)
      .putAllParams(params)
      .build()

    // println(JsonFormat.printer().print(q))

    val whereClause = WhereClauseWithUnnamedParams(new CamelToPascalConverter())
    whereClause.toWhereClause(q) should be("WHERE Name = ?")
    val expected = new util.ArrayList[String]()
    expected.add("value")
    whereClause.toUnnamedParamList(q, paramMapper) should be(expected)
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

    // println(JsonFormat.printer().print(q))

    val whereClause = WhereClauseWithUnnamedParams(new CamelToPascalConverter())
    // println(whereClause.toWhereClause(q, 2))
    whereClause.toWhereClause(q, 2) should be(
      """  WHERE Id = ?
        |  AND Name = ?""".stripMargin)
    val expected = new util.ArrayList[String]()
    expected.add("id_value")
    expected.add("name_value")
    whereClause.toUnnamedParamList(q, paramMapper) should be(expected)
  }

  it should "generate nested predicate with and" in {
    val nestedPredicates = new util.ArrayList[FilterPredicate]()
    val params = new util.HashMap[String, String]()
    nestedPredicates.add(createPredicate(EQ, "id", "id_value", params))
    nestedPredicates.add(createPredicate(EQ, "name", "name_value", params))
    val nestedConnection = createConnection(AND, nestedPredicates)
    val predicates = new util.ArrayList[FilterPredicate]()
    predicates.add(createPredicate(EQ, "type", "type_value", params))
    predicates.add(nestedConnection)
    val connection = createConnection(AND, predicates)

    val q = QueryCommand.newBuilder()
      .setPredicate(connection)
      .putAllParams(params)
      .build()

    // println(JsonFormat.printer().print(q))

    val whereClause = WhereClauseWithUnnamedParams(new CamelToPascalConverter())
    // println(whereClause.toWhereClause(q, 2))
    whereClause.toWhereClause(q, 2) should be(
      """  WHERE Type = ?
        |  AND (Id = ?
        |    AND Name = ?)""".stripMargin)
    val expected = new util.ArrayList[String]()
    expected.add("type_value")
    expected.add("id_value")
    expected.add("name_value")
    whereClause.toUnnamedParamList(q, paramMapper) should be(expected)
  }

  it should "generate IN predicate with param array" in {
    val params = new util.HashMap[String, String]()
    val stringArray = s"""["1", "2"]"""
    val q = QueryCommand.newBuilder()
      .setPredicate(createPredicate(IN, "id", stringArray, params))
      .putAllParams(params)
      .build()

    // println(JsonFormat.printer().print(q))

    val whereClause = WhereClauseWithUnnamedParams(new CamelToPascalConverter())
    // println(whereClause.toWhereClause(q, 2))
    whereClause.toWhereClause(q, 2) should be(
      """  WHERE Id IN (?,?)""".stripMargin)
    val expected = new util.ArrayList[String]()
    expected.add("1")
    expected.add("2")
    whereClause.toUnnamedParamList(q, paramMapper) should be(expected)
  }

  private val paramMapper: QueryParamMapper = new QueryParamMapper {
    override def map(name: String, value: String): AnyRef = value

    override def exists(name: String): Boolean = true
  }
}
