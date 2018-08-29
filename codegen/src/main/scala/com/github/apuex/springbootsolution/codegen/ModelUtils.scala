package com.github.apuex.springbootsolution.codegen

import scala.xml.{Node, Text}

object ModelUtils {
  def isEnum(model: Node, name: String): Boolean = {
    !(model.child.filter(x => x.label == "entity")
      .filter(x => isEnum(x) && x.attribute("name").asInstanceOf[Some[Text]].get.data == name)
      .isEmpty)
  }

  def isEnum(entity: Node): Boolean =
    entity.attribute("enum")
      .map(x => {
        x.filter(n => n.isInstanceOf[Text])
          .map(n => n.asInstanceOf[Text].data == "true")
          .foldLeft(false)(_ || _)
      })
      .getOrElse(false)

  def isAggregationRoot(entity: Node): Boolean =
    entity.attribute("aggregationRoot")
      .map(x => {
        x.filter(n => n.isInstanceOf[Text])
          .map(n => n.asInstanceOf[Text].data == "true")
          .foldLeft(false)(_ || _)
      })
      .getOrElse(false)

  def persistentColumns(entity: Node): Seq[Node] = {
    entity.child.filter(x => x.label == "field")
      .filter(f => {
        !(f.attribute("transient")
          .map(x => {
            x.filter(n => n.isInstanceOf[Text])
              .map(n => n.asInstanceOf[Text].data == "true")
              .foldLeft(false)(_ || _)
          })
          .getOrElse(false))
      })
  }
}
