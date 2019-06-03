package com.github.apuex.springbootsolution.codegen

import scala.xml._

object FilterEntity extends App {
  val xml = ModelLoader(args(0)).xml

  println("<?xml version=\"1.0\"?>")

  val root = xml.asInstanceOf[Elem]
    .copy(
      prefix = xml.prefix,
      label = xml.label, //val label: String,
      attributes = xml.attributes, //: MetaData,
      scope = xml.scope,
      minimizeEmpty = true,
      child = xml.child
        .filter(x => x.label == "entity")
        .filter(x => {
          val criteria = x.\@("name").map(c => c.<=(0x7f)).foldLeft(true)(_ && _)
          if(!criteria) println(s"<!-- non-ascii char in name: ${x.\@("name")} -->")
          criteria
        })
        .filter(x => {
          val criteria = x.\@("name").map(c => c.<('0') || c.>('9')).foldLeft(true)(_ && _)
          if(!criteria) println(s"<!-- numbers in name: ${x.\@("name")} -->")
          criteria
        })
        .filter(x => {
          val criteria = !(x.child.filter(c => c.label == "primaryKey").isEmpty) | !(x.child.filter(c => c.label == "uniqueKey").isEmpty)
          if(!criteria) println(s"<!-- no primary key: ${x.\@("name")} -->")
          criteria
        })
    )

  println(root)
}
