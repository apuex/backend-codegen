package com.github.apuex.springbootsolution.runtime

import com.github.apuex.springbootsolution.runtime.LogicalConnectionType._
import com.github.apuex.springbootsolution.runtime.PredicateType._
import com.google.gson.Gson

import scala.collection.JavaConverters._

object WhereClauseWithUnnamedParams {
  def apply(convert: SymbolConverter): WhereClauseWithUnnamedParams = new WhereClauseWithUnnamedParams(convert)
}

class WhereClauseWithUnnamedParams(c: SymbolConverter) {
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

  def isRoot(q: QueryCommand, connection: LogicalConnectionVo): Boolean = {
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
          s"(${
            connection.getPredicatesList.asScala
              .map(x => toSql(q, x, indent + 2))
              .reduce((x, y) => s"${x}\n${indenting}OR ${y}")
          })"
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
    case EQ => s"${c.convert(predicate.getFieldName)} = ?"
    case NE => s"${c.convert(predicate.getFieldName)} <> ?"
    case LT => s"${c.convert(predicate.getFieldName)} < ?"
    case GT => s"${c.convert(predicate.getFieldName)} > ?"
    case LE => s"${c.convert(predicate.getFieldName)} <= ?"
    case GE => s"${c.convert(predicate.getFieldName)} >= ?"
    case BETWEEN => s"${c.convert(predicate.getFieldName)} BETWEEN ? AND ?"
    case LIKE => s"${c.convert(predicate.getFieldName)} LIKE ?"
    case IS_NULL => s"${c.convert(predicate.getFieldName)} IS NULL"
    case IS_NOT_NULL => s"${c.convert(predicate.getFieldName)} IS NOT NULL"
    case IN => s"${c.convert(predicate.getFieldName)} IN (${toPlaceHolders(q.getParamsMap.get(predicate.getParamNames(0)))})"
    case NOT_IN => s"${c.convert(predicate.getFieldName)} NOT IN (${toPlaceHolders(q.getParamsMap.get(predicate.getParamNames(0)))})"
    case _ => throw new IllegalArgumentException(predicate.toString)
  }

  /**
    * Generate unnamed parameters for using with '?' as place holders.
    *
    * Available for java & scala
    *
    * @param q
    * @return
    */
  def toUnnamedParamList(q: QueryCommand, m: QueryParamMapper): java.util.List[Object] = {
    toUnnamedParams(q, m).asJava
  }

  /**
    * Generate unnamed parameters for using with '?' as place holders.
    *
    * Available for scala
    *
    * @param q
    * @return
    */
  def toUnnamedParams(q: QueryCommand, m: QueryParamMapper): Seq[Object] = {
    toUnnamedParams(q.getPredicate, q.getParamsMap, m)
  }

  /**
    * Generate unnamed parameters for using with '?' as place holders.
    *
    * Available for scala
    *
    * @param criteria
    * @return
    */
  def toUnnamedParams(criteria: FilterPredicate, params: java.util.Map[String, String], m: QueryParamMapper): Seq[Object] = {
    if (criteria.hasConnection) {
      toUnnamedParams(criteria.getConnection, params, m)
    } else if (criteria.hasPredicate) {
      toUnnamedParams(criteria.getPredicate, params, m)
    } else {
      throw new IllegalArgumentException(criteria.toString)
    }
  }

  private def toUnnamedParams(connection: LogicalConnectionVo, params: java.util.Map[String, String], m: QueryParamMapper): Seq[Object] = {
    if (connection.getPredicatesList.isEmpty) {
      Seq()
    } else {
      connection.getPredicatesList.asScala
        .map(x => toUnnamedParams(x, params, m)).reduce((x, y) => x ++ y)
    }
  }

  private def toUnnamedParams(predicate: LogicalPredicateVo, params: java.util.Map[String, String], m: QueryParamMapper): Seq[Object] = predicate.getPredicateType match {
    case EQ => Seq(m.map(predicate.getFieldName(), params.get(predicate.getParamNames(0))))
    case NE => Seq(m.map(predicate.getFieldName(), params.get(predicate.getParamNames(0))))
    case LT => Seq(m.map(predicate.getFieldName(), params.get(predicate.getParamNames(0))))
    case GT => Seq(m.map(predicate.getFieldName(), params.get(predicate.getParamNames(0))))
    case LE => Seq(m.map(predicate.getFieldName(), params.get(predicate.getParamNames(0))))
    case GE => Seq(m.map(predicate.getFieldName(), params.get(predicate.getParamNames(0))))
    case BETWEEN => Seq(m.map(predicate.getFieldName(), params.get(predicate.getParamNames(0))), m.map(predicate.getFieldName(), params.get(predicate.getParamNames(1))))
    case LIKE => Seq(m.map(predicate.getFieldName(), "%%%s%%".format(params.get(predicate.getParamNames(0)))))
    case IS_NULL => Seq()
    case IS_NOT_NULL => Seq()
    case IN => parseStringArray(params.get(predicate.getParamNames(0)))
      .map(x => m.map(predicate.getFieldName(), x)).toSeq
    case NOT_IN => parseStringArray(params.get(predicate.getParamNames(0)))
      .map(x => m.map(predicate.getFieldName(), x)).toSeq
    case _ => throw new IllegalArgumentException(predicate.toString)
  }

  private def parseStringArray(json: String): Array[String] = {
    gson.fromJson(json, classOf[Array[String]])
  }

  private def toPlaceHolders(json: String): String = {
    (0 until gson.fromJson(json, classOf[Array[String]]).length)
      .map(_ => "?")
      .reduce((x, y) => "%s,%s".format(x, y))
  }
}
