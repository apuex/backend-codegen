package com.github.apuex.codegen.runtime

import com.github.apuex.codegen.runtime.Messages.LogicalConnectionType._
import com.github.apuex.codegen.runtime.Messages.PredicateType._
import com.github.apuex.codegen.runtime.Messages._
import com.github.apuex.codegen.runtime.SymbolConverter._

import scala.collection.JavaConverters._

object WhereClauseWithUnnamedParams {
  def apply(convert: Converter): WhereClauseWithUnnamedParams = new WhereClauseWithUnnamedParams(convert)
}

class WhereClauseWithUnnamedParams(convert: Converter) {
  /**
    * Generate SQL WHERE clause from query command.
    *
    * @param q
    * @return
    */
  def toWhereClause(q: QueryCommand): String = {
    toWhereClause(q.getPredicate, 0)
  }

  /**
    * Generate SQL WHERE clause from query command.
    *
    * @param q
    * @param indent
    * @return
    */
  def toWhereClause(q: QueryCommand, indent: Int): String = {
    toWhereClause(q.getPredicate, indent)
  }

  /**
    * Generate SQL WHERE clause from query criteria.
    *
    * @param criteria the criteria for filtering result sets.
    * @param indent   indent count, in blank space character.
    * @return A SQL WHERE clause
    */
  def toWhereClause(criteria: FilterPredicate, indent: Int): String = {
    val indenting = s"${(0 until indent).map(_ => " ").foldLeft("")(_ + _)}"
    s"${indenting}WHERE ${toSql(criteria, indent)}"
  }

  /**
    * Generate SQL filter predicate or compound predicates from query criteria.
    *
    * @param criteria the criteria for filtering result sets.
    * @param indent   indent count, in blank space character.
    * @return A compound predicates for SQL WHERE clause
    */
  def toSql(criteria: FilterPredicate, indent: Int): String = {
    if (criteria.hasConnection) {
      s"${toSql(criteria.getConnection, indent)}"
    } else if (criteria.hasPredicate) {
      s"${toSql(criteria.getPredicate, indent)}"
    } else {
      throw new IllegalArgumentException(criteria.toString)
    }
  }

  /**
    * Generate compound SQL filter predicates from compound predicates connected by logical connective.
    *
    * @param connection the criteria for filtering result sets.
    * @param indent     indent count, in blank space character.
    * @return A compound predicates for SQL WHERE clause
    */
  private def toSql(connection: LogicalConnectionVo, indent: Int): String = {
    val indenting = s"${(0 until indent).map(_ => " ").foldLeft("")(_ + _)}"
    connection.getLogicalConnectionType match {
      case AND =>
        if (connection.getPredicatesList.isEmpty) {
          ""
        } else {
          s"(${
            connection.getPredicatesList.asScala
              .map(x => toSql(x, indent + 2))
              .reduce((x, y) => s"${x}\n${indenting}AND ${y}")
          })"
        }
      case OR =>
        if (connection.getPredicatesList.isEmpty) {
          ""
        } else {
          s"(${
            connection.getPredicatesList.asScala
              .map(x => toSql(x, indent + 2))
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
  private def toSql(predicate: LogicalPredicateVo, indent: Int): String = predicate.getPredicateType match {
    case EQ => s"${convert(predicate.getFieldName)} = ?"
    case NE => s"${convert(predicate.getFieldName)} <> ?"
    case LT => s"${convert(predicate.getFieldName)} < ?"
    case GT => s"${convert(predicate.getFieldName)} > ?"
    case LE => s"${convert(predicate.getFieldName)} <= ?"
    case GE => s"${convert(predicate.getFieldName)} >= ?"
    case BETWEEN => s"${convert(predicate.getFieldName)} BETWEEN ? AND ?"
    case LIKE => s"${convert(predicate.getFieldName)} LIKE ?"
    case IS_NULL => s"${convert(predicate.getFieldName)} IS NULL"
    case IS_NOT_NULL => s"${convert(predicate.getFieldName)} IS NOT NULL"
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
  def toUnnamedParamList(q: QueryCommand): java.util.List[String] = {
    toUnnamedParams(q).asJava
  }

  /**
    * Generate unnamed parameters for using with '?' as place holders.
    *
    * Available for scala
    *
    * @param q
    * @return
    */
  def toUnnamedParams(q: QueryCommand): Seq[String] = {
    toUnnamedParams(q.getPredicate, q.getParamsMap)
  }

  /**
    * Generate unnamed parameters for using with '?' as place holders.
    *
    * Available for scala
    *
    * @param criteria
    * @return
    */
  def toUnnamedParams(criteria: FilterPredicate, params: java.util.Map[String, String]): Seq[String] = {
    if (criteria.hasConnection) {
      toUnnamedParams(criteria.getConnection, params)
    } else if (criteria.hasPredicate) {
      toUnnamedParams(criteria.getPredicate, params)
    } else {
      throw new IllegalArgumentException(criteria.toString)
    }
  }

  private def toUnnamedParams(connection: LogicalConnectionVo, params: java.util.Map[String, String]): Seq[String] = {
    if (connection.getPredicatesList.isEmpty) {
      Seq()
    } else {
      connection.getPredicatesList.asScala.toSeq
        .map(x => toUnnamedParams(x, params)).reduce((x, y) => x ++ y)
    }
  }

  private def toUnnamedParams(predicate: LogicalPredicateVo, params: java.util.Map[String, String]): Seq[String] = predicate.getPredicateType match {
    case EQ => Seq(params.get(predicate.getParamNames(0)))
    case NE => Seq(params.get(predicate.getParamNames(0)))
    case LT => Seq(params.get(predicate.getParamNames(0)))
    case GT => Seq(params.get(predicate.getParamNames(0)))
    case LE => Seq(params.get(predicate.getParamNames(0)))
    case GE => Seq(params.get(predicate.getParamNames(0)))
    case BETWEEN => Seq(params.get(predicate.getParamNames(0)), params.get(predicate.getParamNames(1)))
    case LIKE => Seq(params.get(predicate.getParamNames(0)))
    case IS_NULL => Seq()
    case IS_NOT_NULL => Seq()
    case _ => throw new IllegalArgumentException(predicate.toString)
  }

}
