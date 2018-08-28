package com.github.apuex.springbootsolution.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

import scala.xml.Text

object Project extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${cToShell(modelName)}"

  new File(projectDir).mkdirs()

  project

  private def project = {
    val printWriter = new PrintWriter(s"${projectDir}/pom.xml", "utf-8")

    val source =
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         |         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
         |  <modelVersion>4.0.0</modelVersion>
         |
         |  <groupId>${modelPackage}</groupId>
         |  <artifactId>${cToShell(modelName)}</artifactId>
         |  <version>1.0-SNAPSHOT</version>
         |  <packaging>pom</packaging>
         |
         |  <parent>
         |    <groupId>org.springframework.boot</groupId>
         |    <artifactId>spring-boot-starter-parent</artifactId>
         |    <version>2.0.3.RELEASE</version>
         |  </parent>
         |
         |  <properties>
         |    <java.version>1.8</java.version>
         |  </properties>
         |
         |  <modules>
         |    <module>${cToShell(modelName)}-app</module>
         |    <module>${cToShell(modelName)}-controller</module>
         |    <module>${cToShell(modelName)}-dao</module>
         |    <module>${cToShell(modelName)}-message</module>
         |    <module>${cToShell(modelName)}-service</module>
         |  </modules>
         |
         |  <build>
         |    <plugins>
         |      <plugin>
         |        <groupId>org.apache.maven.plugins</groupId>
         |        <artifactId>maven-compiler-plugin</artifactId>
         |        <version>3.7.0</version>
         |        <configuration>
         |          <source>1.8</source>
         |          <target>1.8</target>
         |          <encoding>UTF-8</encoding>
         |        </configuration>
         |      </plugin>
         |      <!-- walk-around solution for idea cannot import generated code. -->
         |      <plugin>
         |        <groupId>org.apache.maven.plugins</groupId>
         |        <artifactId>maven-resources-plugin</artifactId>
         |        <version>3.1.0</version>
         |        <configuration>
         |          <resources>
         |            <resource>
         |              <directory>target/generated-resources</directory>
         |              <directory>src/main/resources</directory>
         |            </resource>
         |          </resources>
         |        </configuration>
         |      </plugin>
         |      <plugin>
         |        <groupId>org.apache.maven.plugins</groupId>
         |        <artifactId>maven-source-plugin</artifactId>
         |        <version>3.0.1</version>
         |        <executions>
         |          <execution>
         |            <id>attach-sources</id>
         |            <goals>
         |              <goal>jar</goal>
         |            </goals>
         |          </execution>
         |        </executions>
         |      </plugin>
         |    </plugins>
         |  </build>
         |</project>
         |
       """.stripMargin

    printWriter.print(source)

    printWriter.close()
  }
}
