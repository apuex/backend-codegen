package com.github.apuex.springbootsolution.runtime

import java.sql.{Blob, Timestamp}
import java.util.function.Predicate

import com.github.apuex.springbootsolution.runtime.PredicateType._

trait IndexHint {
  def indexed(columns: Array[String]): Boolean
}

trait FieldPredicateMapper[T] {
  def predicate(predicateType: PredicateType, values: Array[String]): Predicate[T]
}

trait QueryPredicateMapper[T] {
  def predicate(name: String, predicateType: PredicateType, values: Array[String]): Predicate[T]
}

object BooleanFilter {
  def filter(predicateType: PredicateType, a: Array[Boolean]): Predicate[Boolean] = predicateType match {
    case EQ => _ == a(0)
    case NE => _ != a(0)
    case LT => _ < a(0)
    case GT => _ > a(0)
    case LE => _ <= a(0)
    case GE => _ >= a(0)
    case BETWEEN => x => x >= a(0) && x <= a(1)
    case LIKE => _ == a(0)
    case IS_NULL => _ == false // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => _ == true // can not compare with null, no real nulls for basic types in proto3
    case IN => x => a.exists(_ == x)
    case NOT_IN => x => !a.exists(_ == x)
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object ByteFilter {
  def filter(predicateType: PredicateType, a: Array[Byte]): Predicate[Byte] = predicateType match {
    case EQ => _ == a(0)
    case NE => _ != a(0)
    case LT => _ < a(0)
    case GT => _ > a(0)
    case LE => _ <= a(0)
    case GE => _ >= a(0)
    case BETWEEN => x => x >= a(0) && x <= a(1)
    case LIKE => _ == a(0)
    case IS_NULL => _ == 0 // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => _ != 0 // can not compare with null, no real nulls for basic types in proto3
    case IN => x => a.exists(_ == x)
    case NOT_IN => x => !a.exists(_ == x)
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object ShortFilter {
  def filter(predicateType: PredicateType, a: Array[Short]): Predicate[Short] = predicateType match {
    case EQ => _ == a(0)
    case NE => _ != a(0)
    case LT => _ < a(0)
    case GT => _ > a(0)
    case LE => _ <= a(0)
    case GE => _ >= a(0)
    case BETWEEN => x => x >= a(0) && x <= a(1)
    case LIKE => _ == a(0)
    case IS_NULL => _ == 0 // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => _ != 0 // can not compare with null, no real nulls for basic types in proto3
    case IN => x => a.exists(_ == x)
    case NOT_IN => x => !a.exists(_ == x)
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object IntFilter {
  def filter(predicateType: PredicateType, a: Array[Int]): Predicate[Int] = predicateType match {
    case EQ => _ == a(0)
    case NE => _ != a(0)
    case LT => _ < a(0)
    case GT => _ > a(0)
    case LE => _ <= a(0)
    case GE => _ >= a(0)
    case BETWEEN => x => x >= a(0) && x <= a(1)
    case LIKE => _ == a(0)
    case IS_NULL => _ == 0 // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => _ != 0 // can not compare with null, no real nulls for basic types in proto3
    case IN => x => a.exists(_ == x)
    case NOT_IN => x => !a.exists(_ == x)
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object LongFilter {
  def filter(predicateType: PredicateType, a: Array[Long]): Predicate[Long] = predicateType match {
    case EQ => _ == a(0)
    case NE => _ != a(0)
    case LT => _ < a(0)
    case GT => _ > a(0)
    case LE => _ <= a(0)
    case GE => _ >= a(0)
    case BETWEEN => x => x >= a(0) && x <= a(1)
    case LIKE => _ == a(0)
    case IS_NULL => _ == 0 // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => _ != 0 // can not compare with null, no real nulls for basic types in proto3
    case IN => x => a.exists(_ == x)
    case NOT_IN => x => !a.exists(_ == x)
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object DoubleFilter {
  def filter(predicateType: PredicateType, a: Array[Double]): Predicate[Double] = predicateType match {
    case EQ => _ == a(0)
    case NE => _ != a(0)
    case LT => _ < a(0)
    case GT => _ > a(0)
    case LE => _ <= a(0)
    case GE => _ >= a(0)
    case BETWEEN => x => x >= a(0) && x <= a(1)
    case LIKE => _ == a(0)
    case IS_NULL => _ == 0 // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => _ != 0 // can not compare with null, no real nulls for basic types in proto3
    case IN => x => a.exists(_ == x)
    case NOT_IN => x => !a.exists(_ == x)
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object FloatFilter {
  def filter(predicateType: PredicateType, a: Array[Float]): Predicate[Float] = predicateType match {
    case EQ => _ == a(0)
    case NE => _ != a(0)
    case LT => _ < a(0)
    case GT => _ > a(0)
    case LE => _ <= a(0)
    case GE => _ >= a(0)
    case BETWEEN => x => x >= a(0) && x <= a(1)
    case LIKE => _ == a(0)
    case IS_NULL => _ == 0 // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => _ != 0 // can not compare with null, no real nulls for basic types in proto3
    case IN => x => a.exists(_ == x)
    case NOT_IN => x => !a.exists(_ == x)
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object TimestampFilter {
  def filter(predicateType: PredicateType, a: Array[Timestamp]): Predicate[Timestamp] = predicateType match {
    case EQ => _.equals(a(0))
    case NE => _.equals(a(0))
    case LT => _.before(a(0))
    case GT => _.after(a(0))
    case LE => x => x.before(a(0)) || x.equals(a(0))
    case GE => x => x.after(a(0)) || x.equals(a(0))
    case BETWEEN => x => x.after(a(0)) || x.equals(a(0)) && x.before(a(1)) || x.equals(a(1))
    case LIKE => _.equals(a(0))
    case IS_NULL => _ == null
    case IS_NOT_NULL => _ != null
    case IN => x => a.exists(_.equals(x))
    case NOT_IN => x => !a.exists(_.equals(x))
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object StringFilter {
  def filter(predicateType: PredicateType, a: Array[String]): Predicate[String] = predicateType match {
    case EQ => _ == a(0)
    case NE => _ != a(0)
    case LT => _ < a(0)
    case GT => _ > a(0)
    case LE => _ <= a(0)
    case GE => _ >= a(0)
    case BETWEEN => x => x >= a(0) && x <= a(1)
    case LIKE => _.contains(a(0))
    case IS_NULL => _ == null
    case IS_NOT_NULL => _ != null
    case IN => x => a.exists(_ == x)
    case NOT_IN => x => !a.exists(_ == x)
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object BlobFilter {
  def filter(predicateType: PredicateType): (Blob, Array[Blob]) => Boolean =
    throw new IllegalArgumentException("filtering on blob fields is not supported.")
}

