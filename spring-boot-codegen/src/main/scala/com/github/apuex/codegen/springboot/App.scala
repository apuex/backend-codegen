package com.github.apuex.codegen.springboot

import java.io.{File, PrintWriter}

import com.github.apuex.codegen.runtime.SymbolConverters._

import scala.xml.{Node, Text}

object App extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${camelToShell(modelName)}/${camelToShell(modelName)}-app"
  val srcDir = s"${projectDir}/src/main/java/${modelPackage.replace('.', '/')}/app"

  new File(srcDir).mkdirs()

  project

  application

  private def application = {
    val printWriter = new PrintWriter(s"${srcDir}/Application.java", "utf-8")

    val source =
      s"""package ${modelPackage}.service;
         |
         |import org.springframework.boot.*;
         |import org.springframework.boot.autoconfigure.*;
         |import org.springframework.context.annotation.*;
         |import org.springframework.http.converter.protobuf.*;
         |
         |@Configuration
         |@ComponentScan({"${modelPackage}.*"})
         |@SpringBootApplication
         |public class Application {
         |
         |  public static void main(String[] args) {
         |    SpringApplication.run(Application.class, args);
         |  }
         |
         |  @Bean
         |  ProtobufHttpMessageConverter protobufHttpMessageConverter() {
         |    return new ProtobufHttpMessageConverter();
         |  }
         |}
    """.stripMargin
    printWriter.print(source)

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
         |  <artifactId>${camelToShell(modelName)}-app</artifactId>
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
         |      <artifactId>controller</artifactId>
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
         |
         |  </dependencies>
         |
         |  <build>
         |    <plugins>
         |      <plugin>
         |        <groupId>org.springframework.boot</groupId>
         |        <artifactId>spring-boot-maven-plugin</artifactId>
         |        <version>2.0.3.RELEASE</version>
         |      </plugin>
         |    </plugins>
         |  </build>
         |
         |</project>
         |
       """.stripMargin

    printWriter.print(source)

    printWriter.close()
  }
}
