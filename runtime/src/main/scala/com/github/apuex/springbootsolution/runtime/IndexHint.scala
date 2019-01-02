package com.github.apuex.springbootsolution.runtime

import java.sql.{Blob, Timestamp}
import java.util
import java.util.function.Predicate

import com.github.apuex.springbootsolution.runtime.PredicateType._

trait IndexHint {
  def indexed(columns: Array[String]): Boolean
}

trait Filter[T] {
  val list: java.util.List[String] = new util.ArrayList[String]()
  val a: Predicate[Int] = x => x > 0
  val p: Predicate[String] = x => x != null
  val q: Predicate[String] = q.and(x => x.length() > 0)
  val r: Predicate[String] = q.or(x => x.length() < 10)

  val result = Array(
    a.test(10),
    r.test("abcd")
  ).foldLeft(true)(_ && _)

  def filter(predicateType: PredicateType): (T, Array[T]) => Boolean
}

object Filter {
  val filters = Map(
    (classOf[Boolean] -> BooleanFilter),
    (classOf[Byte] -> ByteFilter),
    (classOf[Short] -> ShortFilter),
    (classOf[Int] -> IntFilter),
    (classOf[Long] -> LongFilter),
    (classOf[Float] -> FloatFilter),
    (classOf[Double] -> DoubleFilter),
    (classOf[Timestamp] -> TimestampFilter),
    (classOf[String] -> StringFilter),
    (classOf[Blob] -> BlobFilter)
  )

}

object BooleanFilter {
  def filter(predicateType: PredicateType): (Boolean, Array[Boolean]) => Boolean = predicateType match {
    case EQ => (_ == _ (0))
    case NE => (_ != _ (0))
    case LT => (_ < _ (0))
    case GT => (_ > _ (0))
    case LE => (_ <= _ (0))
    case GE => (_ >= _ (0))
    case BETWEEN => ((x, a) => x >= a(0) && x <= a(1))
    case LIKE => ((_, _) => false)
    case IS_NULL => ((x, _) => x == false) // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => ((x, _) => x == true) // can not compare with null, no real nulls for basic types in proto3
    case IN => ((x, a) => a.exists(_ == x))
    case NOT_IN => ((x, a) => !a.exists(_ == x))
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object ByteFilter {
  def filter(predicateType: PredicateType): (Byte, Array[Byte]) => Boolean = predicateType match {
    case EQ => (_ == _ (0))
    case NE => (_ != _ (0))
    case LT => (_ < _ (0))
    case GT => (_ > _ (0))
    case LE => (_ <= _ (0))
    case GE => (_ >= _ (0))
    case BETWEEN => ((x, a) => x >= a(0) && x <= a(1))
    case LIKE => (_ == _ (0))
    case IS_NULL => ((x, _) => x == 0) // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => ((x, _) => x != 0) // can not compare with null, no real nulls for basic types in proto3
    case IN => ((x, a) => a.exists(_ == x))
    case NOT_IN => ((x, a) => !a.exists(_ == x))
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object ShortFilter {
  def filter(predicateType: PredicateType): (Short, Array[Short]) => Boolean = predicateType match {
    case EQ => (_ == _ (0))
    case NE => (_ != _ (0))
    case LT => (_ < _ (0))
    case GT => (_ > _ (0))
    case LE => (_ <= _ (0))
    case GE => (_ >= _ (0))
    case BETWEEN => ((x, a) => x >= a(0) && x <= a(1))
    case LIKE => (_ == _ (0))
    case IS_NULL => ((x, _) => x == 0) // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => ((x, _) => x != 0) // can not compare with null, no real nulls for basic types in proto3
    case IN => ((x, a) => a.exists(_ == x))
    case NOT_IN => ((x, a) => !a.exists(_ == x))
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object IntFilter {
  def filter(predicateType: PredicateType): (Int, Array[Int]) => Boolean = predicateType match {
    case EQ => (_ == _ (0))
    case NE => (_ != _ (0))
    case LT => (_ < _ (0))
    case GT => (_ > _ (0))
    case LE => (_ <= _ (0))
    case GE => (_ >= _ (0))
    case BETWEEN => ((x, a) => x >= a(0) && x <= a(1))
    case LIKE => (_ == _ (0))
    case IS_NULL => ((x, _) => x == 0) // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => ((x, _) => x != 0) // can not compare with null, no real nulls for basic types in proto3
    case IN => ((x, a) => a.exists(_ == x))
    case NOT_IN => ((x, a) => !a.exists(_ == x))
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object LongFilter {
  def filter(predicateType: PredicateType): (Long, Array[Long]) => Boolean = predicateType match {
    case EQ => (_ == _ (0))
    case NE => (_ != _ (0))
    case LT => (_ < _ (0))
    case GT => (_ > _ (0))
    case LE => (_ <= _ (0))
    case GE => (_ >= _ (0))
    case BETWEEN => ((x, a) => x >= a(0) && x <= a(1))
    case LIKE => (_ == _ (0))
    case IS_NULL => ((x, _) => x == 0) // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => ((x, _) => x != 0) // can not compare with null, no real nulls for basic types in proto3
    case IN => ((x, a) => a.exists(_ == x))
    case NOT_IN => ((x, a) => !a.exists(_ == x))
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object DoubleFilter {
  def filter(predicateType: PredicateType): (Double, Array[Double]) => Boolean = predicateType match {
    case EQ => (_ == _ (0))
    case NE => (_ != _ (0))
    case LT => (_ < _ (0))
    case GT => (_ > _ (0))
    case LE => (_ <= _ (0))
    case GE => (_ >= _ (0))
    case BETWEEN => ((x, a) => x >= a(0) && x <= a(1))
    case LIKE => (_ == _ (0))
    case IS_NULL => ((x, _) => x == 0) // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => ((x, _) => x != 0) // can not compare with null, no real nulls for basic types in proto3
    case IN => ((x, a) => a.exists(_ == x))
    case NOT_IN => ((x, a) => !a.exists(_ == x))
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object FloatFilter {
  def filter(predicateType: PredicateType): (Float, Array[Float]) => Boolean = predicateType match {
    case EQ => (_ == _ (0))
    case NE => (_ != _ (0))
    case LT => (_ < _ (0))
    case GT => (_ > _ (0))
    case LE => (_ <= _ (0))
    case GE => (_ >= _ (0))
    case BETWEEN => ((x, a) => x >= a(0) && x <= a(1))
    case LIKE => (_ == _ (0))
    case IS_NULL => ((x, _) => x == 0) // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => ((x, _) => x != 0) // can not compare with null, no real nulls for basic types in proto3
    case IN => ((x, a) => a.exists(_ == x))
    case NOT_IN => ((x, a) => !a.exists(_ == x))
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object TimestampFilter {
  def filter(predicateType: PredicateType): (Timestamp, Array[Timestamp]) => Boolean = predicateType match {
    case EQ => ((x, a) => x.equals(a(0)))
    case NE => ((x, a) => x.equals(a(0)))
    case LT => ((x, a) => x.before(a(0)))
    case GT => ((x, a) => x.after(a(0)))
    case LE => ((x, a) => x.before(a(0)) || x.equals(a(0)))
    case GE => ((x, a) => x.after(a(0)) || x.equals(a(0)))
    case BETWEEN => ((x, a) => x.after(a(0)) || x.equals(a(0)) && x.before(a(1)) || x.equals(a(1)))
    case LIKE => ((x, a) => x.equals(a(0)))
    case IS_NULL => ((x, _) => x == null) // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => ((x, _) => x != null) // can not compare with null, no real nulls for basic types in proto3
    case IN => ((x, a) => a.exists(_.equals(x)))
    case NOT_IN => ((x, a) => !a.exists(_.equals(x)))
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object StringFilter {
  def filter(predicateType: PredicateType): (String, Array[String]) => Boolean = predicateType match {
    case EQ => (_ == _ (0))
    case NE => (_ != _ (0))
    case LT => (_ < _ (0))
    case GT => (_ > _ (0))
    case LE => (_ <= _ (0))
    case GE => (_ >= _ (0))
    case BETWEEN => ((x, a) => x >= a(0) && x <= a(1))
    case LIKE => ((_, _) => false)
    case IS_NULL => ((x, _) => x == false) // can not compare with null, no real nulls for basic types in proto3
    case IS_NOT_NULL => ((x, _) => x == true) // can not compare with null, no real nulls for basic types in proto3
    case IN => ((x, a) => a.exists(_ == x))
    case NOT_IN => ((x, a) => !a.exists(_ == x))
    case _ => throw new IllegalArgumentException(predicateType.toString)
  }
}

object BlobFilter {
  def filter(predicateType: PredicateType): (Blob, Array[Blob]) => Boolean =
    throw new IllegalArgumentException("filtering on blob fields is not supported.")
}

