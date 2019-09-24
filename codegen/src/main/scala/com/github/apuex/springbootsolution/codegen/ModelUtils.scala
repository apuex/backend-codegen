package com.github.apuex.springbootsolution.codegen

import scala.xml.{Node, Text}

object ModelUtils {
  def isEnum(model: Node, name: String): Boolean = {
    !(model.child.filter(x => x.label == "entity")
      .filter(x => isEnum(x) && x.\@("name") == name)
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
    entity.attribute("aggregateRoot")
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

  def persistentColumnsExtended(model: Node, entity: Node): Seq[Node] = {
    val parentEntityName = parentName(entity)
    val parent = parentEntityName.map(x => persistentColumnsExtended(model, entityFor(model, x))).getOrElse(Seq())
    val fkColumnsNames = if(parentEntityName.isDefined) {
      entity.child.filter(x => x.label == "foreignKey" && parentEntityName.map(n => n == x.\@("refEntity")).get)
        .flatMap(k => k.child.filter(x => x.label == "field"))
        .map(f => (f.\@("name"), f.\@("refField")))
        .toMap
    } else {
      entity.child.filter(x => x.label == "foreignKey")
        .flatMap(k => k.child.filter(x => x.label == "field"))
        .map(f => (f.\@("name"), f.\@("refField")))
        .toMap
    }

    val pkFkColumnsNames = entity.child.filter(x => x.label == "primaryKey")
      .flatMap(k => k.child.filter(x => x.label == "field"))
      .map(f => f.\@("name"))
      .filter(f => fkColumnsNames.contains(f))
      .toSet
    val extended = entity.child.filter(x => x.label == "field")
      .filter(f => {
        !(f.attribute("transient")
          .map(x => {
            x.filter(n => n.isInstanceOf[Text])
              .map(n => n.asInstanceOf[Text].data == "true")
              .foldLeft(false)(_ || _)
          })
          .getOrElse(false))
      })
      .filter(f => !pkFkColumnsNames.contains(f.\@("name")))
    return parent ++ extended
  }

  def parentName(entity: Node): Option[String] = {
    entity.attribute("extends")
      .map(x => {
        x.filter(e => e.isInstanceOf[Text])
          .map(e => e.asInstanceOf[Text].data)
          .foldLeft("")(_ + _)
      })
  }

  def extendedEntityNames(model: Node, entity: Node): Seq[String] = {
    val parentEntityName = parentName(entity)
    val parent = parentEntityName.map(x => extendedEntityNames(model, entityFor(model, x))).getOrElse(Seq())
    val extended = Seq(entity.\@("name"))
    return parent ++ extended
  }

  def primaryKeyColumns(model: Node, entity: Node):Seq[Node] = {
    val parentEntityName = parentName(entity)
    val parent = parentEntityName.map(x => primaryKeyColumns(model, entityFor(model, x))).getOrElse(Seq())
    val fkColumnsNames = entity.child.filter(x => x.label == "foreignKey" && parentEntityName.map(n => n == x.\@("refEntity")).get)
      .flatMap(k => k.child.filter(x => x.label == "field"))
      .map(f => (f.\@("name"), f.\@("refField")))
      .toMap
    val pkColumnsNames = entity.child.filter(x => x.label == "primaryKey")
      .flatMap(k => k.child.filter(x => x.label == "field"))
      .map(f => f.\@("name"))
      .filter(f => !fkColumnsNames.contains(f))
      .toSet
    val extended = entity.child.filter(x => x.label == "field")
      .filter(f => pkColumnsNames.contains(f.\@("name")))
    return parent ++ extended
  }

  def joinColumnsForExtension(model: Node, entity: Node): Map[String, String] = {
    val parentEntityName = parentName(entity)
    val parent = parentEntityName.map(x => joinColumnsForExtension(model, entityFor(model, x))).getOrElse(Map())
    val extended = entity.child.filter(x => x.label == "foreignKey" && parentEntityName.map(n => n == x.\@("refEntity")).get)
      .flatMap(k => k.child.filter(x => x.label == "field"))
      .map(f => (f.\@("name"), f.\@("refField")))
      .toMap
    return parent ++ extended
  }

  def entityFor(model: Node, name: String): Node = {
    val matched = model.child.filter(e => e.label == "entity")
      .filter(e => e.\@("name") == name)
    if(!matched.isEmpty) matched(0) else throw new RuntimeException("entity with name %s does not exists.".format(name))
  }
}
