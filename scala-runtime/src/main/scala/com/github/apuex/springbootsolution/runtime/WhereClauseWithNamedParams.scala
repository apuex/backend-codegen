package com.github.apuex.springbootsolution.runtime

import com.github.apuex.springbootsolution.runtime.LogicalConnectionType.{AND, OR}
import com.github.apuex.springbootsolution.runtime.PredicateType.{BETWEEN, EQ, GE, GT, IN, IS_NOT_NULL, IS_NULL, LE, LIKE, LT, NE, NOT_IN}
import com.google.gson.Gson

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
    q.predicate
      .map(p => toWhereClause(q, p, indent))
      .getOrElse("")
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
    if (criteria.clause.isConnection) {
      s"${toSql(q, criteria.getConnection, indent)}"
    } else if (criteria.clause.isPredicate) {
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
    *
    * @param q
    * @param connection
    * @return true if it is root level.
    */
  private def isRoot(q: QueryCommand, connection: LogicalConnectionVo): Boolean = {
    q.getPredicate != null &&
      q.getPredicate.clause.isConnection &&
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
    connection.logicalConnectionType match {
      case AND =>
        if (connection.predicates.isEmpty) {
          ""
        } else {
          if (isRoot(q, connection)) {
            s"${
              connection.predicates
                .map(x => toSql(q, x, indent + 2))
                .reduceOption((x, y) => s"${x}\n${indenting}AND ${y}")
                .getOrElse("")
            }"
          } else {
            s"(${
              connection.predicates
                .map(x => toSql(q, x, indent + 2))
                .reduceOption((x, y) => s"${x}\n${indenting}AND ${y}")
                .getOrElse("")
            })"
          }
        }
      case OR =>
        if (connection.predicates.isEmpty) {
          ""
        } else {
          if (isRoot(q, connection)) {
            s"${
              connection.predicates
                .map(x => toSql(q, x, indent + 2))
                .reduceOption((x, y) => s"${x}\n${indenting}OR ${y}")
                .getOrElse("")
            }"
          } else {
            s"(${
              connection.predicates
                .map(x => toSql(q, x, indent + 2))
                .reduceOption((x, y) => s"${x}\n${indenting}OR ${y}")
                .getOrElse("")
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
  private def toSql(q: QueryCommand, predicate: LogicalPredicateVo, indent: Int): String = predicate.predicateType match {
    case EQ => s"${c.convert(predicate.fieldName)} = {${predicate.paramNames(0)}}"
    case NE => s"${c.convert(predicate.fieldName)} <> {${predicate.paramNames(0)}}"
    case LT => s"${c.convert(predicate.fieldName)} < {${predicate.paramNames(0)}}"
    case GT => s"${c.convert(predicate.fieldName)} > {${predicate.paramNames(0)}}"
    case LE => s"${c.convert(predicate.fieldName)} <= {${predicate.paramNames(0)}}"
    case GE => s"${c.convert(predicate.fieldName)} >= {${predicate.paramNames(0)}}"
    case BETWEEN => s"${c.convert(predicate.fieldName)} BETWEEN {${predicate.paramNames(0)}} AND {${predicate.paramNames(1)}}"
    case LIKE => s"${c.convert(predicate.fieldName)} LIKE {${predicate.paramNames(0)}}"
    case IS_NULL => s"${c.convert(predicate.fieldName)} IS NULL"
    case IS_NOT_NULL => s"${c.convert(predicate.fieldName)} IS NOT NULL"
    case IN => s"${c.convert(predicate.fieldName)} IN ({${predicate.paramNames(0)}})"
    case NOT_IN => s"${c.convert(predicate.fieldName)} NOT IN ({${predicate.paramNames(0)}})"
    case _ => throw new IllegalArgumentException(predicate.toString)
  }

  def toNamedParams(criteria: FilterPredicate, params: Map[String, String]): Seq[(String, String, Any)] = {
    if (criteria.clause.isConnection) {
      toNamedParams(criteria.getConnection, params)
    } else if (criteria.clause.isPredicate) {
      toNamedParams(criteria.getPredicate, params)
    } else {
      throw new IllegalArgumentException(criteria.toString)
    }
  }

  def toNamedParams(predicate: LogicalPredicateVo, params: Map[String, String]): Seq[(String, String, Any)] = predicate.predicateType match {
    case EQ => Seq((predicate.fieldName, predicate.paramNames(0), params(predicate.paramNames(0))))
    case NE => Seq((predicate.fieldName, predicate.paramNames(0), params(predicate.paramNames(0))))
    case LT => Seq((predicate.fieldName, predicate.paramNames(0), params(predicate.paramNames(0))))
    case GT => Seq((predicate.fieldName, predicate.paramNames(0), params(predicate.paramNames(0))))
    case LE => Seq((predicate.fieldName, predicate.paramNames(0), params(predicate.paramNames(0))))
    case GE => Seq((predicate.fieldName, predicate.paramNames(0), params(predicate.paramNames(0))))
    case BETWEEN =>
      Seq(
        (predicate.fieldName, predicate.paramNames(0), params(predicate.paramNames(0))),
        (predicate.fieldName, predicate.paramNames(1), params(predicate.paramNames(1)))
      )
    case LIKE => Seq((predicate.fieldName, predicate.paramNames(0), s"%${params(predicate.paramNames(0))}%"))
    case IS_NULL => Seq()
    case IS_NOT_NULL => Seq()
    case IN => Seq((predicate.fieldName, predicate.paramNames(0), parseStringArray(params(predicate.paramNames(0)))))
    case NOT_IN => Seq((predicate.fieldName, predicate.paramNames(0), parseStringArray(params(predicate.paramNames(0)))))
    case _ => throw new IllegalArgumentException(predicate.toString)
  }

  def toNamedParams(connection: LogicalConnectionVo, params: Map[String, String]): Seq[(String, String, Any)] = connection.logicalConnectionType match {
    case AND =>
      connection.predicates.map(x => toNamedParams(x, params)).foldLeft(Seq[(String, String, Any)]())((x, y) => x ++ y)
    case OR =>
      connection.predicates.map(x => toNamedParams(x, params)).foldLeft(Seq[(String, String, Any)]())((x, y) => x ++ y)
    case _ => throw new IllegalArgumentException(connection.toString)
  }

  def parseStringArray(json: String): Array[String] = {
    gson.fromJson(json, classOf[Array[String]])
  }

  def orderBy(orderBys: Seq[OrderBy], alias: String=""): String = {
    val aliasPrefix = if("" == alias.trim) "" else s"${alias}."
    orderBys
      .map(x => {
        s"""
           |${aliasPrefix}${x.fieldName} ${x.order.name}
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")
  }
}
