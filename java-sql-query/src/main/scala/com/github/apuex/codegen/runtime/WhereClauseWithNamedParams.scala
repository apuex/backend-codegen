package com.github.apuex.codegen.runtime

import SymbolConverters._
import com.github.apuex.codegen.runtime.Messages._
import com.github.apuex.codegen.runtime.Messages.PredicateType._
import com.github.apuex.codegen.runtime.Messages.LogicalConnectionType._
import scala.collection.JavaConverters._

object WhereClauseWithNamedParams {
  def apply(convert: Converter): WhereClauseWithNamedParams = new WhereClauseWithNamedParams(convert)
  def apply(convert: SymbolConverter): WhereClauseWithNamedParams =
    new WhereClauseWithNamedParams({ case s: String => convert.convert(s)})
}

class WhereClauseWithNamedParams(convert: Converter) {
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
    * @param criteria the criteria for filtering result sets.
    * @param indent   indent count, in blank space character.
    * @return A compound predicates for SQL WHERE clause
    */
  def toSql(criteria: FilterPredicate, indent:Int): String = {
    if(criteria.hasConnection) {
      s"${toSql(criteria.getConnection, indent)}"
    } else if(criteria.hasPredicate) {
      s"${toSql(criteria.getPredicate, indent)}"
    } else {
      throw new IllegalArgumentException(criteria.toString)
    }
  }

  /**
    * Generate compound SQL filter predicates from compound predicates connected by logical connective.
    * @param connection the criteria for filtering result sets.
    * @param indent     indent count, in blank space character.
    * @return A compound predicates for SQL WHERE clause
    */
  private def toSql(connection: LogicalConnectionVo, indent:Int): String = {
    val indenting = s"${(0 until indent).map(_ => " ").foldLeft("")(_ + _)}"
    connection.getLogicalConnectionType match {
      case AND =>
        if (connection.getPredicatesList.isEmpty) {
          ""
        } else {
          s"(${connection.getPredicatesList.asScala
            .map(x => toSql(x, indent + 2))
            .reduce((x, y) => s"${x}\n${indenting}AND ${y}")})"
        }
      case OR =>
        if (connection.getPredicatesList.isEmpty) {
          ""
        } else {
          s"(${connection.getPredicatesList.asScala
            .map(x => toSql(x, indent + 2))
            .reduce((x, y) => s"${x}\n${indenting}OR ${y}")})"
        }
      case _ => throw new IllegalArgumentException(connection.toString)
    }
  }

  /**
    * Generate SQL predicate from input predicate object
    * @param predicate the input predicate object
    * @param indent    indent count, in blank space character.
    * @return A predicate for SQL WHERE clause
    */
  private def toSql(predicate: LogicalPredicateVo, indent: Int): String = predicate.getPredicateType match {
    case EQ => s"${convert(predicate.getFieldName)} = {${predicate.getParamNames(0)}}"
    case NE => s"${convert(predicate.getFieldName)} <> {${predicate.getParamNames(0)}}"
    case LT => s"${convert(predicate.getFieldName)} < {${predicate.getParamNames(0)}}"
    case GT => s"${convert(predicate.getFieldName)} > {${predicate.getParamNames(0)}}"
    case LE => s"${convert(predicate.getFieldName)} <= {${predicate.getParamNames(0)}}"
    case GE => s"${convert(predicate.getFieldName)} >= {${predicate.getParamNames(0)}}"
    case BETWEEN => s"${convert(predicate.getFieldName)} BETWEEN {${predicate.getParamNames(0)}} AND {${predicate.getParamNames(1)}}"
    case LIKE => s"${convert(predicate.getFieldName)} LIKE {${predicate.getParamNames(0)}}"
    case IS_NULL => s"${convert(predicate.getFieldName)} IS NULL"
    case IS_NOT_NULL => s"${convert(predicate.getFieldName)} IS NOT NULL"
    case _ => throw new IllegalArgumentException(predicate.toString)
  }

}
