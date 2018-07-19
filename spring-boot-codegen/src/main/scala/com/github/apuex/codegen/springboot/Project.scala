package com.github.apuex.codegen.springboot

import java.io.{File, PrintWriter}

import com.github.apuex.codegen.runtime.SymbolConverters._

import scala.xml.Text

object Project extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${camelToShell(modelName)}"

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
         |  <artifactId>${camelToShell(modelName)}</artifactId>
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
         |    <module>${camelToShell(modelName)}-app</module>
         |    <module>controller</module>
         |    <module>dao</module>
         |    <module>message</module>
         |    <module>service</module>
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
