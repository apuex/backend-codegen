package com.github.apuex.codegen.springboot

import java.io.{File, PrintWriter}

import com.github.apuex.codegen.runtime.SymbolConverters._

import scala.xml._

object Controller extends App {

  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${camelToShell(modelName)}/controller"
  val srcDir = s"${projectDir}/src/main/java/${modelPackage.replace('.', '/')}/controller"

  new File(srcDir).mkdirs()

  project

  xml.child.filter(x => x.label == "entity")
    .filter(x => x.attribute("aggregationRoot") == Some(Text("true")))
    .foreach(x => controllerForEntity(modelPackage, x))

  private def controllerForEntity(modelPackage: String, entity: Node): Unit = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val prelude =
    s"""package ${modelPackage}.controller;
      |import com.github.apuex.codegen.runtime.Messages.*;
      |import ${modelPackage}.message.${camelToPascal(modelName)}.*;
      |import ${modelPackage}.service.*;
      |import org.springframework.beans.factory.annotation.*;
      |import org.springframework.web.bind.annotation.*;
      |
      |import java.util.*;
      |
      |@RestController
      |@RequestMapping(value="${pascalToShell(entityName)}", method=RequestMethod.POST)
      |public class ${entityName}Controller {
      |  @Autowired
      |  private ${entityName}Service service;
      |
      |  @RequestMapping(value="create-${pascalToShell(entityName)}")
      |  public void create(@RequestBody Create${entityName}Cmd c) {
      |    service.create(c);
      |  }
      |
      |  @RequestMapping(value="retrieve-${pascalToShell(entityName)}")
      |  public ${entityName}Vo retrieve(@RequestBody Retrieve${entityName}Cmd c) {
      |    return service.retrieve(c);
      |  }
      |  @RequestMapping(value="update-${pascalToShell(entityName)}")
      |  public void update(@RequestBody Update${entityName}Cmd c) {
      |    service.update(c);
      |  }
      |
      |  @RequestMapping(value="delete-${pascalToShell(entityName)}")
      |  public void delete(@RequestBody Delete${entityName}Cmd c) {
      |    service.delete(c);
      |  }
      |
      |  @RequestMapping(value="query-${pascalToShell(entityName)}")
      |  public List<${entityName}Vo> query(@RequestBody QueryCommand q) {
      |    return service.query(q);
      |  }
      |
      |""".stripMargin

    val end =
      """}
        |""".stripMargin

    val printWriter = new PrintWriter(s"${srcDir}/${entityName}Controller.java", "utf-8")

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
         |  <artifactId>controller</artifactId>
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
         |      <artifactId>service</artifactId>
         |      <version>1.0-SNAPSHOT</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework.boot</groupId>
         |      <artifactId>spring-boot-starter-web</artifactId>
         |      <version>2.0.3.RELEASE</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework.boot</groupId>
         |      <artifactId>spring-boot-starter-test</artifactId>
         |      <version>2.0.3.RELEASE</version>
         |      <scope>test</scope>
         |    </dependency>
         |    <dependency>
         |      <groupId>com.jayway.jsonpath</groupId>
         |      <artifactId>json-path</artifactId>
         |      <version>2.4.0</version>
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
