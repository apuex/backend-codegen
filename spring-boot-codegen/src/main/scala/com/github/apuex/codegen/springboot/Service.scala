package com.github.apuex.codegen.springboot

import java.io.{File, PrintWriter}

import com.github.apuex.codegen.runtime.SymbolConverters._
import com.github.apuex.codegen.runtime.TextUtils._
import com.github.apuex.codegen.springboot.App.{modelName, modelPackage, projectDir}

import scala.xml.{Node, Text}

object Service extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${camelToShell(modelName)}/service"
  val srcDir = s"${projectDir}/src/main/java/${modelPackage.replace('.', '/')}/service"

  new File(srcDir).mkdirs()

  project

  xml.child.filter(x => x.label == "entity")
    .filter(x => x.attribute("aggregationRoot") == Some(Text("true")))
    .foreach(x => serviceForEntity(modelPackage, x))

  private def serviceForEntity(modelPackage: String, entity: Node): Unit = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val prelude =
      s"""package ${modelPackage}.service;
         |
         |import com.github.apuex.codegen.runtime.*;
         |import com.github.apuex.codegen.runtime.Messages.*;
         |import ${modelPackage}.message.${camelToPascal(modelName)}.*;
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
         |public class ${entityName}Service {
         |  @Autowired
         |  private ${entityName}DAO ${pascalToCamel(entityName)}DAO;
         |
         |  @Transactional
         |  public void create(Create${entityName}Cmd c) {
         |    ${pascalToCamel(entityName)}DAO.create(c);
         |  }
         |
         |  @Transactional
         |  public ${entityName}Vo retrieve(Retrieve${entityName}Cmd c) {
         |    return ${pascalToCamel(entityName)}DAO.retrieve(c);
         |  }
         |
         |  @Transactional
         |  public void update(Update${entityName}Cmd c) {
         |    ${pascalToCamel(entityName)}DAO.update(c);
         |  }
         |
         |  @Transactional
         |  public void delete(Delete${entityName}Cmd c) {
         |    ${pascalToCamel(entityName)}DAO.delete(c);
         |  }
         |
         |  @Transactional
         |  public List<${entityName}Vo> query(QueryCommand q) {
         |    return ${pascalToCamel(entityName)}DAO.query(q);
         |  }
         |
         |""".stripMargin

    val end =
      """}
        |""".stripMargin

    val printWriter = new PrintWriter(s"${srcDir}/${entityName}Service.java", "utf-8")

    printWriter.print(prelude)
    printWriter.print(end)

    printWriter.close()
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
         |  <artifactId>service</artifactId>
         |  <version>1.0-SNAPSHOT</version>
         |
         |  <parent>
         |    <groupId>${modelPackage}</groupId>
         |    <artifactId>${camelToShell(modelName)}</artifactId>
         |    <version>1.0-SNAPSHOT</version>
         |  </parent>
         |
         |  <dependencies>
         |    <dependency>
         |      <groupId>${modelPackage}</groupId>
         |      <artifactId>dao</artifactId>
         |      <version>1.0-SNAPSHOT</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework.boot</groupId>
         |      <artifactId>spring-boot-starter-jdbc</artifactId>
         |      <version>2.0.3.RELEASE</version>
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
