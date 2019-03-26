package com.github.apuex.springbootsolution.runtime

import com.github.apuex.springbootsolution.runtime.PredicateType._
import com.github.apuex.springbootsolution.runtime.LogicalConnectionType._
import com.google.gson.Gson

import scala.collection.JavaConverters._

object WhereClauseWithNamedParams {
  def apply(convert: SymbolConverter): WhereClauseWithNamedParams = new WhereClauseWithNamedParams(convert)
}

class WhereClauseWithNamedParams(c: SymbolConverter) {
  val gson = new Gson()
  /**
    * Generate SQL WHERE clause from query command.
    *
    * @param q
    * @return
    */
  def toWhereClause(q: QueryCommand): String = {
    toWhereClause(q, q.getPredicate, 0)
  }

  /**
    * Generate SQL WHERE clause from query command.
    *
    * @param q
    * @param indent
    * @return
    */
  def toWhereClause(q: QueryCommand, indent: Int): String = {
    toWhereClause(q, q.getPredicate, indent)
  }

  /**
    * Generate SQL WHERE clause from query criteria.
    *
    * @param criteria the criteria for filtering result sets.
    * @param indent   indent count, in blank space character.
    * @return A SQL WHERE clause
    */
  def toWhereClause(q: QueryCommand, criteria: FilterPredicate, indent: Int): String = {
    val indenting = s"${(0 until indent).map(_ => " ").foldLeft("")(_ + _)}"
    s"${indenting}WHERE ${toSql(q, criteria, indent)}"
  }

  /**
    * Generate SQL filter predicate or compound predicates from query criteria.
    *
    * @param criteria the criteria for filtering result sets.
    * @param indent   indent count, in blank space character.
    * @return A compound predicates for SQL WHERE clause
    */
  def toSql(q: QueryCommand, criteria: FilterPredicate, indent: Int): String = {
    if (criteria.hasConnection) {
      s"${toSql(q, criteria.getConnection, indent)}"
    } else if (criteria.hasPredicate) {
      s"${toSql(q, criteria.getPredicate, indent)}"
    } else {
      throw new IllegalArgumentException(criteria.toString)
    }
  }

  /**
    * Check if logical connection is root level.
    *
    * Some database system cannot accept root level filter predicates parenthesized,
    * therefore, predicates cannot be parenthesized by default for simplicity.
    * @param q
    * @param connection
    * @return true if it is root level.
    */
  private def isRoot(q: QueryCommand, connection: LogicalConnectionVo): Boolean = {
    q.getPredicate != null &&
      q.getPredicate.hasConnection &&
      q.getPredicate.getConnection.equals(connection)
  }

  /**
    * Generate compound SQL filter predicates from compound predicates connected by logical connective.
    *
    * @param connection the criteria for filtering result sets.
    * @param indent     indent count, in blank space character.
    * @return A compound predicates for SQL WHERE clause
    */
  private def toSql(q: QueryCommand, connection: LogicalConnectionVo, indent: Int): String = {
    val indenting = s"${(0 until indent).map(_ => " ").foldLeft("")(_ + _)}"
    connection.getLogicalConnectionType match {
      case AND =>
        if (connection.getPredicatesList.isEmpty) {
          ""
        } else {
          if(isRoot(q, connection)) {
            s"${
              connection.getPredicatesList.asScala
                .map(x => toSql(q, x, indent + 2))
                .reduce((x, y) => s"${x}\n${indenting}AND ${y}")
            }"
          } else {
            s"(${
              connection.getPredicatesList.asScala
                .map(x => toSql(q, x, indent + 2))
                .reduce((x, y) => s"${x}\n${indenting}AND ${y}")
            })"
          }
        }
      case OR =>
        if (connection.getPredicatesList.isEmpty) {
          ""
        } else {
          if(isRoot(q, connection)) {
            s"${
              connection.getPredicatesList.asScala
                .map(x => toSql(q, x, indent + 2))
                .reduce((x, y) => s"${x}\n${indenting}OR ${y}")
            }"
          } else {
            s"(${
              connection.getPredicatesList.asScala
                .map(x => toSql(q, x, indent + 2))
                .reduce((x, y) => s"${x}\n${indenting}OR ${y}")
            })"
          }
        }
      case _ => throw new IllegalArgumentException(connection.toString)
    }
  }

  /**
    * Generate SQL predicate from input predicate object
    *
    * @param predicate the input predicate object
    * @param indent    indent count, in blank space character.
    * @return A predicate for SQL WHERE clause
    */
  private def toSql(q: QueryCommand, predicate: LogicalPredicateVo, indent: Int): String = predicate.getPredicateType match {
    case EQ => s"${c.convert(predicate.getFieldName)} = {${predicate.getParamNames(0)}}"
    case NE => s"${c.convert(predicate.getFieldName)} <> {${predicate.getParamNames(0)}}"
    case LT => s"${c.convert(predicate.getFieldName)} < {${predicate.getParamNames(0)}}"
    case GT => s"${c.convert(predicate.getFieldName)} > {${predicate.getParamNames(0)}}"
    case LE => s"${c.convert(predicate.getFieldName)} <= {${predicate.getParamNames(0)}}"
    case GE => s"${c.convert(predicate.getFieldName)} >= {${predicate.getParamNames(0)}}"
    case BETWEEN => s"${c.convert(predicate.getFieldName)} BETWEEN {${predicate.getParamNames(0)}} AND {${predicate.getParamNames(1)}}"
    case LIKE => s"${c.convert(predicate.getFieldName)} LIKE {${predicate.getParamNames(0)}}"
    case IS_NULL => s"${c.convert(predicate.getFieldName)} IS NULL"
    case IS_NOT_NULL => s"${c.convert(predicate.getFieldName)} IS NOT NULL"
    case IN => s"${c.convert(predicate.getFieldName)} IN ({${predicate.getParamNames(0)}})"
    case NOT_IN => s"${c.convert(predicate.getFieldName)} NOT IN ({${predicate.getParamNames(0)}})"
    case _ => throw new IllegalArgumentException(predicate.toString)
  }

  def toNamedParams(criteria: FilterPredicate, params: Map[String, String]): Seq[(String, String, String)] = {
  if (criteria.hasConnection) {
    toNamedParams(criteria.getConnection, params)
  } else if (criteria.hasPredicate) {
    toNamedParams(criteria.getPredicate, params)
  } else {
  throw new IllegalArgumentException(criteria.toString)
  }
  }

  def toNamedParams(predicate: LogicalPredicateVo, params: Map[String, String]): Seq[(String, String, String)] = predicate.getPredicateType match {
    case EQ => Seq((predicate.getFieldName, predicate.getParamNames(0), params(predicate.getParamNames(0))))
    case NE => Seq((predicate.getFieldName, predicate.getParamNames(0), params(predicate.getParamNames(0))))
    case LT => Seq((predicate.getFieldName, predicate.getParamNames(0), params(predicate.getParamNames(0))))
    case GT => Seq((predicate.getFieldName, predicate.getParamNames(0), params(predicate.getParamNames(0))))
    case LE => Seq((predicate.getFieldName, predicate.getParamNames(0), params(predicate.getParamNames(0))))
    case GE => Seq((predicate.getFieldName, predicate.getParamNames(0), params(predicate.getParamNames(0))))
    case BETWEEN =>
      Seq(
        (predicate.getFieldName, predicate.getParamNames(0), params(predicate.getParamNames(0))),
        (predicate.getFieldName, predicate.getParamNames(1), params(predicate.getParamNames(1)))
      )
    case LIKE => Seq((predicate.getFieldName, predicate.getParamNames(0), s"%${params(predicate.getParamNames(0))}%"))
    case IS_NULL => Seq()
    case IS_NOT_NULL => Seq()
    case IN =>  Seq((predicate.getFieldName, predicate.getParamNames(0), params(predicate.getParamNames(0))))
    case NOT_IN =>  Seq((predicate.getFieldName, predicate.getParamNames(0), params(predicate.getParamNames(0))))
    case _ => throw new IllegalArgumentException(predicate.toString)
  }

  def toNamedParams(connection: LogicalConnectionVo, params: Map[String, String]): Seq[(String, String, String)] = connection.getLogicalConnectionType match {
    case AND =>
      connection.getPredicatesList.asScala.map(x => toNamedParams(x, params)).foldLeft(Seq[(String, String, String)]())((x, y) => x ++ y)
    case OR =>
      connection.getPredicatesList.asScala.map(x => toNamedParams(x, params)).foldLeft(Seq[(String, String, String)]())((x, y) => x ++ y)
    case _ => throw new IllegalArgumentException(connection.toString)
  }

  def parseStringArray(json: String): Array[String] = {
    gson.fromJson(json, classOf[Array[String]])
  }
}
