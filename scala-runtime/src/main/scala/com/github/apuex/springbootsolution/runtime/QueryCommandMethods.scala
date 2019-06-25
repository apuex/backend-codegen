package com.github.apuex.springbootsolution.runtime

import java.util.Date

import com.github.apuex.springbootsolution.runtime.FilterPredicate.Clause.{Connection, Predicate}
import com.github.apuex.springbootsolution.runtime.LogicalConnectionType.AND
import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import com.google.protobuf.util.Timestamps

object QueryCommandMethods {

  def andCommand(params: Map[String, Any]): QueryCommand = {
    QueryCommand(
      Some(
        FilterPredicate(
          Connection(
            LogicalConnectionVo(
              AND,
              params.keys.map(equalsPredicate(_)).toSeq
            )
          )
        )
      ),
      params.map(x => (x._1 -> stringValue(x._2)))
    )
  }

  def equalsPredicate(name: String): FilterPredicate = FilterPredicate(
    Predicate(
      LogicalPredicateVo(
        PredicateType.EQ,
        name,
        Seq(name)
      )
    )
  )

  def stringValue: Any => String = {
    case x: Date => Timestamps.toString(Timestamps.fromMillis(x.getTime))
    case x: Timestamp => Timestamps.toString(x)
    case x: timestamp.Timestamp => Timestamps.toString(timestamp.Timestamp.toJavaProto(x))
    case x => x.toString
  }

  def createConnection(connectionType: LogicalConnectionType, predicates: Seq[FilterPredicate]): FilterPredicate = {
    val connectionVo = LogicalConnectionVo(connectionType, predicates)

    FilterPredicate(FilterPredicate.Clause.Connection(connectionVo))
  }

  def createPredicate(predicateType: PredicateType, name: String, value: String): (FilterPredicate, Map[String, String]) = {
    val paramNames = Seq(name)
    val params = Map(name -> value)

    val predicateVo = LogicalPredicateVo(predicateType, name, paramNames)

    (FilterPredicate(FilterPredicate.Clause.Predicate(predicateVo)), params)
  }
}
