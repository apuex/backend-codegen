package com.github.apuex.codegen.runtime

import SymbolConverter._
import com.github.apuex.codegen.runtime.PredicateType._
import com.github.apuex.codegen.runtime.LogicalConnectionType._
import scala.collection.JavaConverters._

object SqlWhereClause {
  def apply(convert: Converter): SqlWhereClause = new SqlWhereClause(convert)
}

class SqlWhereClause(convert: Converter) {
  /**
    * Generate SQL WHERE clause from query command.
    *
    * @param q
    * @return
    */
  def toWhereClause(q: QueryCommand): String = {
    toWhereClause(q.predicate, 0)
  }

  /**
    * Generate SQL WHERE clause from query command.
    *
    * @param q
    * @param indent
    * @return
    */
  def toWhereClause(q: QueryCommand, indent: Int): String = {
    toWhereClause(q.predicate, indent)
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
    if(criteria.clause.isConnection) {
      criteria.clause.connection.map(c => s"${toSql(c, indent)}").get
    } else if(criteria.clause.isPredicate) {
      criteria.clause.predicate.map(p => s"${toSql(p, indent)}").get
    } else {
      throw new IllegalArgumentException(criteria.clause.toString)
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
    connection match {
      case LogicalConnectionVo(AND, predicates) =>
        if (predicates.isEmpty) {
          ""
        } else {
          s"(${predicates.map(x => toSql(x, indent + 2)).reduce((x, y) => s"${x}\n${indenting}AND ${y}")})"
        }
      case LogicalConnectionVo(OR, predicates) =>
        if (predicates.isEmpty) {
          ""
        } else {
          s"(${predicates.map(x => toSql(x, indent + 2)).reduce((x, y) => s"${x}\n${indenting}OR ${y}")})"
        }
      case x@LogicalConnectionVo(_, _) => throw new IllegalArgumentException(x.toString)
    }
  }

  /**
    * Generate SQL predicate from input predicate object
    * @param predicate the input predicate object
    * @param indent    indent count, in blank space character.
    * @return A predicate for SQL WHERE clause
    */
  private def toSql(predicate: LogicalPredicateVo, indent: Int): String = predicate match {
    case LogicalPredicateVo(EQ, fieldName, paramNames) => s"${convert(fieldName)} = {${paramNames(0)}}"
    case LogicalPredicateVo(NE, fieldName, paramNames) => s"${convert(fieldName)} <> {${paramNames(0)}}"
    case LogicalPredicateVo(LT, fieldName, paramNames) => s"${convert(fieldName)} < {${paramNames(0)}}"
    case LogicalPredicateVo(GT, fieldName, paramNames) => s"${convert(fieldName)} > {${paramNames(0)}}"
    case LogicalPredicateVo(LE, fieldName, paramNames) => s"${convert(fieldName)} <= {${paramNames(0)}}"
    case LogicalPredicateVo(GE, fieldName, paramNames) => s"${convert(fieldName)} >= {${paramNames(0)}}"
    case LogicalPredicateVo(BETWEEN, fieldName, paramNames) => s"${convert(fieldName)} BETWEEN {${paramNames(0)}} AND {${paramNames(1)}}"
    case LogicalPredicateVo(LIKE, fieldName, paramNames) => s"${convert(fieldName)} LIKE {${paramNames(0)}}"
    case LogicalPredicateVo(IS_NULL, fieldName, _) => s"${convert(fieldName)} IS NULL"
    case LogicalPredicateVo(IS_NOT_NULL, fieldName, _) => s"${convert(fieldName)} IS NOT NULL"
    case x@LogicalPredicateVo(_, _, _) => throw new IllegalArgumentException(x.toString)
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
    toUnnamedParams(q.predicate)
  }

  /**
    * Generate unnamed parameters for using with '?' as place holders.
    *
    * Available for scala
    *
    * @param criteria
    * @return
    */
  def toUnnamedParams(criteria: FilterPredicate): Seq[String] = {
    if(criteria.clause.isConnection) {
      criteria.clause.connection.map(c => toUnnamedParams(c)).get
    } else if(criteria.clause.isPredicate) {
      criteria.clause.predicate.map(p => toUnnamedParams(p)).get
    } else {
      throw new IllegalArgumentException(criteria.clause.toString)
    }
  }

  private def toUnnamedParams(connection: LogicalConnectionVo): Seq[String] = {
    connection match {
      case LogicalConnectionVo(AND, predicates) =>
        if (predicates.isEmpty) {
          Seq()
        } else {
          predicates.map(x => toUnnamedParams(x)).reduce((x, y) => x ++ y)
        }
      case LogicalConnectionVo(OR, predicates) =>
        if (predicates.isEmpty) {
          Seq()
        } else {
          predicates.map(x => toUnnamedParams(x)).reduce((x, y) => x ++ y)
        }
      case x@LogicalConnectionVo(_, _) => throw new IllegalArgumentException(x.toString)
    }
  }

  private def toUnnamedParams(predicate: LogicalPredicateVo): Seq[String] = predicate match {
    case LogicalPredicateVo(EQ, fieldName, paramNames) => Seq(paramNames(0))
    case LogicalPredicateVo(NE, fieldName, paramNames) => Seq(paramNames(0))
    case LogicalPredicateVo(LT, fieldName, paramNames) => Seq(paramNames(0))
    case LogicalPredicateVo(GT, fieldName, paramNames) => Seq(paramNames(0))
    case LogicalPredicateVo(LE, fieldName, paramNames) => Seq(paramNames(0))
    case LogicalPredicateVo(GE, fieldName, paramNames) => Seq(paramNames(0))
    case LogicalPredicateVo(BETWEEN, fieldName, paramNames) => Seq(paramNames(0), paramNames(1))
    case LogicalPredicateVo(LIKE, fieldName, paramNames) => Seq(paramNames(0))
    case LogicalPredicateVo(IS_NULL, fieldName, _) => Seq()
    case LogicalPredicateVo(IS_NOT_NULL, fieldName, _) => Seq()
    case x@LogicalPredicateVo(_, _, _) => throw new IllegalArgumentException(x.toString)
  }

}
