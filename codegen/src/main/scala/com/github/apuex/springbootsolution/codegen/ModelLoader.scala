package com.github.apuex.springbootsolution.codegen

import java.io._

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

import scala.xml.parsing._
import scala.xml._
import ModelLoader._
import com.github.apuex.springbootsolution.runtime.TypeConverters._

object ModelLoader {
  def apply(fileName: String): ModelLoader = {
    val factory = new NoBindingFactoryAdapter
    new ModelLoader(factory.load(fileName), fileName)
  }

  def apply(xml: Node): ModelLoader = new ModelLoader(xml, "")
  
  case class Field(name: String, _type: String, length: Int, scale: Int, required: Boolean, entity: String, keyField: String, valueField: String, keyType: String, valueType: String, aggregate: Boolean, transient: Boolean, comment: String)

  case class PrimaryKey(name: String, fields: Seq[Field], generated: Boolean = false)

  case class ForeignKeyField(name: String, refField: String, required: Boolean)

  case class ForeignKey(name: String, refEntity: String, fields: Seq[ForeignKeyField])

  case class Message(name: String, fields: Seq[Field], primaryKey: PrimaryKey, transient: Boolean, returnType: String, creator: Boolean)

  case class Aggregate(name: String, root: Boolean, fields: Seq[Field], aggregates: Seq[Aggregate], messages: Seq[Message], primaryKey: PrimaryKey, foreignKeys: Seq[ForeignKey], transient: Boolean)

  case class ValueObject(name: String, fields: Seq[Field], primaryKey: PrimaryKey, foreignKeys: Seq[ForeignKey], transient: Boolean)

  case class EnumOption(value: Int, name: String, label: String)

  case class Enumeration(name: String, options: Seq[EnumOption])

}

class ModelLoader(val xml: Node, val modelFileName: String) {
  val modelName = xml.\@("name")
  val modelPackage = xml.\@("package")
  val projectRoot = s"${System.getProperty("output.dir", "target/generated")}"
  def projectDir(name: String) = s"${projectRoot}/${cToShell(modelName)}/${cToShell(modelName)}-${name}"
  def srcDir(name: String) = s"${projectDir(name)}/src/main/java/${modelPackage.replace('.', '/')}/${name}"
  val symboConverter = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}")
    "new IdentityConverter()" else "new CamelToCConverter()"
  val docsDir = s"${projectRoot}/${cToShell(modelName)}/docs"
  val hyphen = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-"

  val model = "model"
  val message = "message"
  val event = "event"
  val apply: String = "apply"
  val api = "api"
  val domain = "domain"
  val query = "query"
  val cluster = "cluster"
  val shard = "sharding"
  val dao = "dao"
  val mysql = "mysql"
  val service = "service"
  val crud = "crud"
  val impl = "impl"
  val app: String = "app"
  val loader: String = "loader"
  //val modelName = xml.\@("name")
  //val modelPackage = xml.\@("package")
  val modelVersion = xml.\@("version")
  val modelMaintainer = xml.\@("maintainer")
  val modelDbSchema = xml.\@("dbSchema")
  val journalTable = xml.\@("journalTable")
  val outputDir = s"${System.getProperty("output.dir", "target/generated")}"
  val rootProjectName = s"${cToShell(modelName)}"
  val rootProjectDir = s"${outputDir}/${rootProjectName}"
  val modelProjectName = s"${cToShell(modelName)}-${model}"
  val modelProjectDir = s"${rootProjectDir}/${model}"
  val modelSrcPackage = s"${modelPackage}"
  val modelTestSrcDir = s"${modelProjectDir}/src/test/scala/${modelSrcPackage.replace('.', '/')}"
  val messageProjectName = s"${cToShell(modelName)}-${message}"
  val messageProjectDir = s"${rootProjectDir}/${message}"
  val messageSrcPackage = s"${modelPackage}"
  val messageSrcDir = s"${messageProjectDir}/src/main/scala/${messageSrcPackage.replace('.', '/')}"
  val messageProtoDir = s"${messageProjectDir}/src/main/protobuf"
  val apiProjectName = s"${cToShell(modelName)}-${api}"
  val apiProjectDir = s"${rootProjectDir}/${api}"
  val apiSrcPackage = s"${modelPackage}"
  val apiSrcDir = s"${apiProjectDir}/src/main/scala/${apiSrcPackage.replace('.', '/')}"
  val domainProjectName = s"${cToShell(modelName)}-${domain}"
  val domainProjectDir = s"${rootProjectDir}/${domain}"
  val domainSrcPackage = s"${modelPackage}.${domain}"
  val domainSrcDir = s"${domainProjectDir}/src/main/scala/${modelPackage.replace('.', '/')}"
  val clusterProjectName = s"${cToShell(modelName)}-${cluster}"
  val clusterProjectDir = s"${rootProjectDir}/${cluster}"
  val clusterSrcPackage = s"${modelPackage}.${shard}"
  val clusterSrcDir = s"${clusterProjectDir}/src/main/scala/${clusterSrcPackage.replace('.', '/')}"
  val daoProjectName = s"${cToShell(modelName)}-${dao}"
  val daoProjectDir = s"${rootProjectDir}/${dao}"
  val daoSrcPackage = s"${modelPackage}.${dao}"
  val daoSrcDir = s"${daoProjectDir}/src/main/scala/${daoSrcPackage.replace('.', '/')}"
  val daoMysqlProjectName = s"${cToShell(modelName)}-${dao}-${mysql}"
  val daoMysqlProjectDir = s"${rootProjectDir}/${dao}-${mysql}"
  val daoMysqlSrcPackage = s"${modelPackage}.${dao}.${mysql}"
  val daoMysqlSrcDir = s"${daoMysqlProjectDir}/src/main/scala/${daoMysqlSrcPackage.replace('.', '/')}"
  val daoMysqlResDir = s"${daoMysqlProjectDir}/src/main/resources"
  val crudImplProjectName = s"${cToShell(modelName)}-${crud}-${impl}"
  val crudImplProjectDir = s"${rootProjectDir}/${crud}-${impl}"
  val crudImplSrcPackage = s"${modelPackage}.${impl}"
  val crudImplSrcDir = s"${crudImplProjectDir}/src/main/scala/${crudImplSrcPackage.replace('.', '/')}"
  val implProjectName = s"${cToShell(modelName)}-${impl}"
  val implProjectDir = s"${rootProjectDir}/${impl}"
  val implSrcPackage = s"${modelPackage}.${impl}"
  val implSrcDir = s"${implProjectDir}/src/main/scala/${implSrcPackage.replace('.', '/')}"
  val appProjectName = s"${cToShell(modelName)}-${cToShell(app)}"
  val appProjectDir = s"${rootProjectDir}/${app}"
  val appProjectConfDir = s"${appProjectDir}/conf"
  val crudAppProjectName = s"${cToShell(modelName)}-${cToShell(crud)}-${cToShell(app)}"
  val crudAppProjectDir = s"${rootProjectDir}/${crud}-${app}"
  val crudAppProjectConfDir = s"${crudAppProjectDir}/conf"
  //val hyphen = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-"

  val entityNames = xml.child
    .filter(x => x.label == "entity" && "true" != x.\@("enum"))
    .map(_.\@("name"))
    .toSet

  val nonEnumNames = xml.child
    .filter(x => x.label == "entity" && "true" != x.\@("enum"))
    .map(x => x.\@("name") +:
      (x.child.filter(_.label == "aggregate")
        .map(_.\@("name")) ++
        x.child.filter(f => f.label == "field" && "true" == f.\@("aggregate"))
          .map(_.\@("name")))
    )
    .flatMap(x => x)
    .toSet

  val enumNames = xml.child.filter(x => x.label == "entity" && "true" == x.\@("enum"))
    .map(_.\@("name"))
    .toSet


  def getEntity(name: String, root: Node): Node = {
    root.child.filter(x => x.label == "entity" && name == x.\@("name")).head
  }

  def toField(node: Node): Field = {
    val name = node.\@("name")
    val _type = node.\@("type")
    val length = if ("" == node.\@("length")) 0 else node.\@("length").toInt
    val scale = if ("" == node.\@("scale")) 0 else node.\@("scale").toInt
    val required = if ("true" == node.\@("required")) true else false
    val entity = node.\@("entity")
    val keyField = node.\@("keyField")
    val valueField = node.\@("valueField")
    val keyType = node.\@("keyType")
    val valueType = node.\@("valueType")
    val aggregate = if ("true" == node.\@("aggregate")) true else false
    val transient = if ("true" == node.\@("transient")) true else false
    val comment = node.\@("comment")
    Field(name, _type, length, scale, required, entity, keyField, valueField, keyType, valueType, aggregate, transient, comment)
  }

  def getFieldNames(node: Node): Seq[String] = {
    node.child.filter(_.label == "field")
      .map(_.\@("name"))
  }

  def getFields(node: Node, root: Node): Seq[Field] = {
    val foreignKeys = getForeignKeys(node)
    val referenced = foreignKeys
      .flatMap(x => x.fields.map(f => getReferencedColumn(f.name, foreignKeys, root)))
      .map(_.get)

    val defined = node.child.filter(_.label == "field")
      .map(x => x.\@("type") match {
        case "" =>
          val name = x.\@("name")
          val refKey = x.\@("refKey")
          val refField = x.\@("refField")
          val refEntity = foreignKeys.filter(_.name == refKey)
            .map(_.refEntity)
          if (refEntity.isEmpty) {
            println(s"name = ${name}")
            println(s"refKey = ${refKey}")
            println(s"refField = ${refField}")
          }
          getReferencedColumn(name, refKey, refEntity.head, refField, root)
        case _ => Some(toField(x))
      })
      .map(_.get) // throws java.util.NoSuchElementException if the option is empty.

    val all = defined ++ referenced

    all
  }

  def getPrimaryKey(node: Node, root: Node): PrimaryKey = {
    val pks = node.child.filter(_.label == "primaryKey")
    if (pks.isEmpty) {
      val aggregatesTo = node.\@("aggregatesTo")
      getPrimaryKey(root.child.filter(x => x.label == "entity" && x.\@("name") == aggregatesTo).head, root)
    } else {
      val pk = pks.head
      val pkName = pk.\@("name")
      val generated = if ("true" == pk.\@("generated")) true else false
      val pkColumnNames = pk.child.filter(_.label == "field")
        .map(_.\@("name"))

      val foreignKeys = getForeignKeys(node)

      val fields = getFields(node, root)
        .map(x => (x.name -> x))
        .toMap

      val pkColumns = pkColumnNames
        .map(x => {
          fields.getOrElse(x, getReferencedColumn(x, foreignKeys, root).get)
        })
        .map(x => Field(x.name, x._type, x.length, x.scale, true, x.entity, x.keyField, x.valueField, x.keyType, x.valueType, x.aggregate, x.transient, x.comment))

      PrimaryKey(pkName, pkColumns, generated)
    }
  }

  def getReferencedColumn(name: String, foreignKeys: Seq[ForeignKey], root: Node): Option[Field] = {
    //println(s"getReferencedColumn: ${name}, ${foreignKeys}")
    val (fkField, refField) = foreignKeys
      .map(k => (k, k.fields.filter(_.name == name)))
      .filter(x => !x._2.isEmpty)
      .map(x => (x._1, x._2.head))
      .map(x => (x, getReferencedColumn(x._2.name, x._1.name, x._1.refEntity, x._2.refField, root)))
      .map(x => (x._1._2, x._2))
      .head

    // disable aggregate attribute on referenced columns
    refField
      .map(x => Field(fkField.name, x._type, x.length, x.scale, fkField.required, x.entity, x.keyField, x.valueField, x.keyType, x.valueType, false, x.transient, x.comment))
  }

  def getReferencedColumn(name: String, refKey: String, refEntity: String, refField: String, root: Node): Option[Field] = {
    val node = root.child.filter(x => x.label == "entity" && x.\@("name") == refEntity).head
    Some(getFields(node, root)
      .filter(_.name == refField).head)
      // disable aggregate attribute on referenced columns
      .map(x => Field(name, x._type, x.length, x.scale, x.required, x.entity, x.keyField, x.valueField, x.keyType, x.valueType, false, x.transient, x.comment))
  }

  def getForeignKeys(node: Node): Seq[ForeignKey] = {
    node.child.filter(_.label == "foreignKey")
      .map(x => {
        ForeignKey(
          x.\@("name"),
          x.\@("refEntity"),
          x.child.filter(_.label == "field")
            .map(f => ForeignKeyField(f.\@("name"), f.\@("refField"), if ("true" == f.\@("required")) true else false))
        )
      })
  }

  def shuffleFields(fields: Seq[Field], pkFields: Seq[Field]): Seq[Field] = {
    val pkNames = pkFields.map(_.name).toSet
    pkFields ++ fields.filter(x => !pkNames.contains(x.name))
  }

  def toAggregate(node: Node, parentFields: Seq[Field], primaryKey: PrimaryKey, root: Node): Aggregate = {
    val transient = if ("true" == node.\@("transient")) true else false
    val fieldNames = getFieldNames(node).toSet
    val fields = parentFields.filter(x => fieldNames.contains(x.name))
    Aggregate(
      node.\@("name"),
      false,
      shuffleFields(primaryKey.fields ++ fields, primaryKey.fields),
      Seq(),
      Seq(),
      primaryKey,
      Seq(),
      transient
    )
  }

  def toAggregate(field: Field, primaryKey: PrimaryKey, root: Node): Aggregate = {
    Aggregate(
      field.name,
      false,
      shuffleFields(primaryKey.fields :+ field, primaryKey.fields),
      Seq(),
      Seq(),
      primaryKey,
      Seq(),
      field.transient
    )
  }

  def toMessage(node: Node, fields: Seq[Field], primaryKey: PrimaryKey, root: Node): Message = {
    val name = node.\@("name")
    val transient = if ("true" == node.\@("transient")) true else false

    val keyFieldNames = primaryKey.fields.map(_.name).toSet
    val parentFieldNames = fields.map(_.name).toSet

    val fieldNames = node.child.filter(x => x.label == "field").map(_.\@("name")).toSet

    val derivedFields = fields.filter(x => fieldNames.contains(x.name))
    val extendedFields = node.child.filter(x => x.label == "field" && !parentFieldNames.contains(x.\@("name")))
      .map(toField)
      .filter(x => !keyFieldNames.contains(x.name))

    val messageFields = derivedFields ++ extendedFields

    ModelLoader.Message(
      name,
      shuffleFields(primaryKey.fields ++ messageFields, primaryKey.fields),
      primaryKey,
      transient,
      node.\@("returnType"),
      if("true" == node.\@("creator")) true else false
    )
  }

  def toAggregate(node: Node, root: Node): Aggregate = {
    val primaryKey = getPrimaryKey(node, root)
    val primaryKeyNames = primaryKey.fields.map(_.name).toSet
    val fields = shuffleFields(getFields(node, root), primaryKey.fields)
      .map(x => if (primaryKeyNames.contains(x.name)) Field(x.name, x._type, x.length, x.scale, true, x.entity, x.keyField, x.valueField, x.keyType, x.valueType, x.aggregate, x.transient, x.comment) else x)
    val aggregates = node.child.filter(_.label == "aggregate").map(toAggregate(_, fields, primaryKey, root)) ++
      fields.filter(_.aggregate)
        .map(x => toAggregate(x, primaryKey, root))
    val foreignKeys = getForeignKeys(node)
      .map(f => {
        ForeignKey(
          f.name,
          f.refEntity,
          f.fields.map(x => if (primaryKeyNames.contains(x.name)) ForeignKeyField(x.name, x.refField, true) else x)
        )
      })

    Aggregate(
      node.\@("name"),
      if ("true" == node.\@("root")) true else false,
      fields,
      aggregates,
      node.child.filter(_.label == "message").map(toMessage(_, fields, primaryKey, root)),
      primaryKey,
      foreignKeys,
      if ("true" == node.\@("transient")) true else false
    )
  }

  def toValueObject(node: Node, aggregatesTo: String, root: Node): ValueObject = {
    val primaryKey = getPrimaryKey(node, root)
    val primaryKeyNames = primaryKey.fields.map(_.name).toSet
    val fields = shuffleFields(getFields(node, root), primaryKey.fields)
      .map(x => if (primaryKeyNames.contains(x.name)) Field(x.name, x._type, x.length, x.scale, true, x.entity, x.keyField, x.valueField, x.keyType, x.valueType, x.aggregate, x.transient, x.comment) else x)
    val foreignKeys = getForeignKeys(node)
      .map(f => {
        ForeignKey(
          f.name,
          f.refEntity,
          f.fields.map(x => if (primaryKeyNames.contains(x.name)) ForeignKeyField(x.name, x.refField, true) else x)
        )
      })

    ValueObject(
      node.\@("name"),
      fields,
      primaryKey,
      foreignKeys,
      if ("true" == node.\@("transient")) true else false
    )
  }

  def toEnumeration(node: Node, aggregatesTo: String, root: Node): Enumeration = {
    val enumDef = node.child.filter(_.label == "enum").head
    val valueField = enumDef.\@("valueField")
    val nameField = enumDef.\@("nameField")
    val labelField = enumDef.\@("labelField")
    val options = node.child.filter(_.label == "row")
      .map(x => {
        EnumOption(
          x.\@(valueField).toInt,
          x.\@(nameField),
          x.\@(labelField)
        )
      })
    Enumeration(
      node.\@("name"),
      options
    )
  }

  def depends(x: Node, y: Node): Boolean = {
    !x.child.filter(_.label == "foreignKey")
      .filter(p => p.\@("refEntity") == y.\@("name"))
      .isEmpty
  }


  def wrapOption(name: String, required: Boolean): String = {
    if (required) name else s"Option[${name}]"
  }

  def wrapOptionValue(valueType: String, value: String, required: Boolean): String = valueType match {
    case "bool" =>
      if (required) value else s"${value}.getOrElse(false)"
    case "short" =>
      if (required) value else s"${value}.getOrElse(0)"
    case "byte" =>
      if (required) value else s"${value}.getOrElse(0)"
    case "int" =>
      if (required) value else s"${value}.getOrElse(0)"
    case "long" =>
      if (required) value else s"${value}.getOrElse(0)"
    case "decimal" =>
      if (required) value else s"${value}.getOrElse(0)"
    case "string" =>
      if (required) value else s"""${value}.getOrElse("")"""
    case "timestamp" =>
      if (required) s"Some(toScalapbTimestamp(${value}))" else s"${value}.map(toScalapbTimestamp(_))"
    case "float" =>
      if (required) value else s"${value}.getOrElse(0)"
    case "double" =>
      if (required) value else s"${value}.getOrElse(0)"
    case "blob" =>
      if (required) s"ByteString.readFrom(${value})" else s"${value}.map(ByteString.readFrom(_)).getOrElse(ByteString.EMPTY)"
    case _ => ""
  }

  def substituteMethodParams(fields: Seq[Field], alias: String = ""): String = {
    val t = if ("" == alias) "" else s"${alias}."
    fields
      .map(x => s"${t}${cToCamel(x.name)}")
      .reduceOption((l, r) => s"${l}, ${r}")
      .getOrElse("")
  }

  def save(fileName: String, content: String, dir: String): Unit = {
    new File(dir).mkdirs()
    val pw = new PrintWriter(new File(dir, fileName), "utf-8")
    pw.println(content)
    pw.close()
  }


  def isAggregateEntity(name: String): Boolean = nonEnumNames.contains(name)

  def isEntity(name: String): Boolean = entityNames.contains(name)

  def isEnum(name: String): Boolean = !nonEnumNames.contains(name) && enumNames.contains(name)

  def defFieldType(name: String): String = {
    if (isAggregateEntity(name)) s"${cToPascal(name)}Vo"
    else cToPascal(toJavaType(name))
  }

  def defFieldType(field: Field): String = {
    // recursive array/map definition is not supported.
    if ("array" == field._type)
      s"""
         |Seq[${defFieldType(field.valueType)}]
       """.stripMargin.trim
    else if ("map" == field._type)
      s"""
         |Map[${defFieldType(field.keyType)}, ${defFieldType(field.valueType)}]
       """.stripMargin.trim
    else if ("any" == field._type)
      s"Option[${defFieldType(field._type)}]"
    else if ("timestamp" == field._type)
      s"Option[${defFieldType(field._type)}]"
    else if (isEnum(field._type))
      cToPascal(field._type)
    else {
      //if (field.required)
      defFieldType(field._type)
      //else
      //  s"Option[${defFieldType(field._type)}]"
    }
  }

  def defaultValue(field: Field): String = {
    if ("array" == field._type)
      "Seq()"
    else if ("map" == field._type)
      "Map()"
    else if ("any" == field._type)
      "None"
    else if ("timestamp" == field._type)
      "None"
    else {
      // if (field.required) {
      defaultValue(field._type)
      //} else
      //  s"None"
    }
  }

  def defaultValue(valueType: String): String = valueType match {
    case "bool" => "false"
    case "short" => "0"
    case "byte" => "0"
    case "int" => "0"
    case "long" => "0"
    case "decimal" => "0"
    case "string" => "\"\""
    case "timestamp" => "None"
    case "float" => "0"
    case "double" => "0"
    case "blob" => "ByteString.EMPTY"
    case x => if (isEnum(x)) s"${cToPascal(x)}.fromValue(0)" else ""
  }

  def defMethodParams(fields: Seq[Field]): String = {
    fields
      .map(x => {
        s"""
           |${cToCamel(x.name)}: ${defFieldType(x)}
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l}, ${r}")
      .getOrElse("")
  }

  def defEntityField(field: Field): String = {
    s"""
       |var ${cToCamel(field.name)}: ${defFieldType(field)} = ${defaultValue(field)}
     """.stripMargin.trim
  }

  def defEntityFields(fields: Seq[Field]): String = {
    fields
      .map(defEntityField)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def addToField(field: Field, aliasPrefix: String): String = {
    if ("array" == field._type) {
      s"""
         |${cToCamel(field.name)} = ${cToCamel(field.name)} ++ ${aliasPrefix}${cToCamel(field.name)}
     """.stripMargin.trim
    } else if ("map" == field._type) {
      s"""
         |${cToCamel(field.name)} = ${cToCamel(field.name)} ++ ${aliasPrefix}${cToCamel(field.name)}
     """.stripMargin.trim
    } else {
      s"""
         |
     """.stripMargin.trim
    }
  }

  def removeFromField(field: Field, aliasPrefix: String): String = {
    if ("array" == field._type) {
      s"""
         |${cToCamel(field.name)} = ${cToCamel(field.name)}.filter(${aliasPrefix}${cToCamel(field.name)}.contains(_))
     """.stripMargin.trim
    } else if ("map" == field._type) {
      s"""
         |${cToCamel(field.name)} = ${cToCamel(field.name)} -- ${aliasPrefix}${cToCamel(field.name)}.keys
     """.stripMargin.trim
    } else {
      s"""
         |
     """.stripMargin.trim
    }
  }

  def updateField(field: Field, aliasPrefix: String): String = {
    s"""
       |${cToCamel(field.name)} = ${aliasPrefix}${cToCamel(field.name)}
     """.stripMargin.trim
  }

  def updateFields(fields: Seq[Field], alias: String = ""): String = {
    fields
      .map(updateField(_, if ("" == alias) "" else s"${alias}."))
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

}
