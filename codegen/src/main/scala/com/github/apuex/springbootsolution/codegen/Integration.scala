package com.github.apuex.springbootsolution.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.codegen.ModelUtils._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._

import scala.xml.{Node, Text}

object Integration extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${cToShell(modelName)}/${cToShell(modelName)}-integration"
  val srcDir = s"${projectDir}/src/main/java/${modelPackage.replace('.', '/')}/integration"

  new File(srcDir).mkdirs()

  project

  integrations(modelPackage, xml)

  def integrations(modelPackage: String, xml: Node): Unit = {
    xml.child.filter(x => x.label == "entity")
      .filter(x => x.attribute("aggregationRoot") == Some(Text("true")))
      .foreach(x => integrationForEntity(xml, modelPackage, x))
  }

  private def integrationForEntity(model: Node, modelPackage: String, entity: Node): Unit = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val prelude =
      s"""package ${modelPackage}.integration;
         |
         |import com.github.apuex.springbootsolution.runtime.*;
         |import ${modelPackage}.message.*;
         |import ${modelPackage}.dao.*;
         |
         |import org.slf4j.*;
         |import org.springframework.beans.factory.annotation.*;
         |import org.springframework.stereotype.*;
         |import org.springframework.transaction.annotation.*;
         |
         |import java.util.*;
         |
         |@Component
         |public class ${cToPascal(entityName)}Integration {
         |  private final static Logger logger = LoggerFactory.getLogger(${cToPascal(entityName)}Integration.class);
         |  ${indent(daoReferences(entityName, entity), 2)}
         |
         |  @Transactional
         |  public void create(Create${cToPascal(entityName)}Cmd c) {
         |    ${indent(create(model, entityName, entity), 4)}
         |  }
         |
         |  @Transactional
         |  public ${cToPascal(entityName)}Vo retrieve(Retrieve${cToPascal(entityName)}Cmd c) {
         |    return ${cToCamel(entityName)}DAO.retrieve(c);
         |  }
         |
         |  @Transactional
         |  public void update(Update${cToPascal(entityName)}Cmd c) {
         |    ${indent(update(model, entityName, entity), 4)}
         |  }
         |
         |  @Transactional
         |  public void delete(Delete${cToPascal(entityName)}Cmd c) {
         |    ${indent(delete(model, entityName, entity), 4)}
         |  }
         |
         |  @Transactional
         |  public ${cToPascal(entityName)}ListVo query(QueryCommand q) {
         |    return ${cToCamel(entityName)}DAO.query(q);
         |  }
         |
         |""".stripMargin

    val end =
      """}
        |""".stripMargin

    val printWriter = new PrintWriter(s"${srcDir}/${cToPascal(entityName)}Integration.java", "utf-8")

    printWriter.print(prelude)
    printWriter.print(end)

    printWriter.close()
  }

  private def daoReferences(entityName: String, entity: Node): String = {
    val parentNameOpts = parentName(entity)
    val parent = s"${parentNameOpts.map(x => String.format("@Autowired\nprivate %sDAO %sDAO;", cToPascal(x), cToCamel(x))).getOrElse("")}"
    val extended = s"""@Autowired
    |private ${cToPascal(entityName)}DAO ${cToCamel(entityName)}DAO;""".stripMargin

    parentNameOpts.map(_ => "%s\n%s".format(parent, extended))
      .getOrElse(extended)
  }

  private def create(model: Node, entityName: String, entity: Node): String = {
    val parentNameOpts = parentName(entity)
    val parent = parentNameOpts.map(x => {
      val parentEntity = entityFor(model, x)
      val columns = persistentColumns(parentEntity)
        .map(f => f.\@("name"))
        .map(f => ".set%s(c.get%s())".format(cToPascal(f), cToPascal(f)))
        .reduceOption((x, y) => "%s\n%s".format(x, y))
        .getOrElse("")
      s"""Create${cToPascal(x)}Cmd cp = Create${cToPascal(x)}Cmd.newBuilder()
         |${indent(columns, 2, true)}
         |.build();
         |${cToCamel(x)}DAO.create(cp);""".stripMargin
    }).getOrElse("")
    val extended = s"${cToCamel(entityName)}DAO.create(c);"

    parentNameOpts.map(_ => "%s\n%s".format(parent, extended))
      .getOrElse(extended)
  }

  private def update(model: Node, entityName: String, entity: Node): String = {
    val parentNameOpts = parentName(entity)
    val parent = parentNameOpts.map(x => {
      val parentEntity = entityFor(model, x)
      val columns = persistentColumns(parentEntity)
        .map(f => f.\@("name"))
        .map(f => ".set%s(c.get%s())".format(cToPascal(f), cToPascal(f)))
        .reduceOption((x, y) => "%s\n%s".format(x, y))
        .getOrElse("")
      s"""Update${cToPascal(x)}Cmd cp = Update${cToPascal(x)}Cmd.newBuilder()
         |${indent(columns, 2, true)}
         |.build();
         |${cToCamel(x)}DAO.update(cp);""".stripMargin
    }).getOrElse("")
    val extended = s"${cToCamel(entityName)}DAO.update(c);"

    parentNameOpts.map(_ => "%s\n%s".format(parent, extended))
      .getOrElse(extended)
  }

  private def delete(model: Node, entityName: String, entity: Node): String = {
    val parentNameOpts = parentName(entity)
    val parent = parentNameOpts.map(x => {
      val parentEntity = entityFor(model, x)
      val columns = joinColumnsForExtension(model, entity)
        .map(f => f._2)
        .map(f => ".set%s(c.get%s())".format(cToPascal(f), cToPascal(f)))
        .reduceOption((x, y) => "%s\n%s".format(x, y))
        .getOrElse("")
      s"""Delete${cToPascal(x)}Cmd cp = Delete${cToPascal(x)}Cmd.newBuilder()
         |${indent(columns, 2, true)}
         |.build();
         |${cToCamel(x)}DAO.delete(cp);""".stripMargin
    }).getOrElse("")
    val extended = s"${cToCamel(entityName)}DAO.delete(c);"

    parentNameOpts.map(_ => "%s\n%s".format(extended, parent))
      .getOrElse(extended)
  }

  private def project = {
    val printWriter = new PrintWriter(s"${projectDir}/pom.xml", "utf-8")

    val source =
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         |         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
         |  <modelVersion>4.0.0</modelVersion>
         |
         |  <groupId>${modelPackage}</groupId>
         |  <artifactId>${cToShell(modelName)}-integration</artifactId>
         |  <version>1.0-SNAPSHOT</version>
         |
         |  <parent>
         |    <groupId>${modelPackage}</groupId>
         |    <artifactId>${cToShell(modelName)}</artifactId>
         |    <version>1.0-SNAPSHOT</version>
         |  </parent>
         |
         |  <dependencies>
         |    <dependency>
         |      <groupId>${modelPackage}</groupId>
         |      <artifactId>${cToShell(modelName)}-dao</artifactId>
         |      <version>1.0-SNAPSHOT</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework</groupId>
         |      <artifactId>spring-jms</artifactId>
         |      <version>5.0.7.RELEASE</version>
         |      <scope>compile</scope>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework.boot</groupId>
         |      <artifactId>spring-boot-starter-test</artifactId>
         |      <version>2.0.3.RELEASE</version>
         |      <scope>test</scope>
         |    </dependency>
         |  </dependencies>
         |
         |</project>
         |
       """.stripMargin

    printWriter.print(source)

    printWriter.close()
  }
}
