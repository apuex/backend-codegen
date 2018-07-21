package com.github.apuex.springbootsolution.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

import scala.xml.{Node, Text}

object App extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${cToShell(modelName)}/${cToShell(modelName)}-app"
  val srcDir = s"${projectDir}/src/main/java/${modelPackage.replace('.', '/')}/app"
  val resourcesDir = s"${projectDir}/src/main/resources"

  new File(srcDir).mkdirs()
  new File(resourcesDir).mkdirs()

  project

  application
  configuration

  private def configuration = {
    val printWriter = new PrintWriter(s"${resourcesDir}/application.properties", "utf-8")

    val source =
      s"""# data source and mq configurations.
         |spring.datasource.url=jdbc:mysql://localhost:3306/example?useSSL=false
         |spring.datasource.username=example
         |spring.datasource.password=password
         |    """.stripMargin

     printWriter.print(source)

     printWriter.close()
  }

  private def application = {
    val printWriter = new PrintWriter(s"${srcDir}/Application.java", "utf-8")

    val source =
      s"""package ${modelPackage}.app;
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
         |  <artifactId>${cToShell(modelName)}-app</artifactId>
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
         |      <artifactId>${cToShell(modelName)}-controller</artifactId>
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
