package com.github.apuex.codegen.runtime

import scala.collection.JavaConverters._
/**
  * Created by wangxy on 17-8-22.
  */
object CollectionCoverter {
  def toImmutableScalaMap[A, B](jm: java.util.Map[A, B]): Map[A, B] = {
    jm.asScala.toMap
  }
}
