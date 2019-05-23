package com.github.apuex.springbootsolution.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.codegen.ModelUtils.persistentColumnsExtended
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._

import scala.xml._

object Controller extends App {

  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${cToShell(modelName)}/${cToShell(modelName)}-controller"
  val srcDir = s"${projectDir}/src/main/java/${modelPackage.replace('.', '/')}/controller"
  val hyphen = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-"

  new File(srcDir).mkdirs()

  project

  controllers(modelPackage, xml)

  def controllers(modelPackage: String, xml: Node): Unit = {
    xml.child.filter(x => x.label == "entity")
      .filter(x => x.attribute("aggregationRoot") == Some(Text("true")))
      .foreach(x => controllerForEntity(modelPackage, x))
  }

  private def controllerForEntity(modelPackage: String, entity: Node): Unit = {
    val entityName = entity.\@("name")
    val columns = persistentColumnsExtended(xml, entity)
        .map(f => (f.\@("name"), f.\@("type")))

    val prelude =
    s"""package ${modelPackage}.controller;
      |
      |import com.github.apuex.springbootsolution.runtime.*;
      |import static com.github.apuex.springbootsolution.runtime.DateFormat.*;
      |import ${modelPackage}.message.*;
      |import ${modelPackage}.service.*;
      |import org.apache.poi.hssf.usermodel.*;
      |import org.apache.poi.hssf.util.*;
      |import org.apache.poi.ss.usermodel.*;
      |import org.springframework.beans.factory.annotation.*;
      |import org.springframework.web.bind.annotation.*;
      |import javax.servlet.http.*;
      |import java.net.*;
      |import java.io.*;
      |import java.util.*;
      |
      |@RestController
      |@RequestMapping(value="${cToShell(entityName)}", method=RequestMethod.POST)
      |public class ${cToPascal(entityName)}Controller {
      |  @Autowired
      |  private ${cToPascal(entityName)}Service service;
      |
      |  @RequestMapping(value="${cToShell("%s%s%s".format("create", hyphen, cToShell(entityName)))}", produces="application/json")
      |  public void create(@RequestBody Create${cToPascal(entityName)}Cmd c, HttpServletRequest r) throws URISyntaxException {
      |    service.create(c, r.getUserPrincipal(), new URI(r.getRequestURI()));
      |  }
      |
      |  @RequestMapping(value="${cToShell("%s%s%s".format("retrieve", hyphen, cToShell(entityName)))}", produces="application/json")
      |  public ${cToPascal(entityName)}Vo retrieve(@RequestBody Retrieve${cToPascal(entityName)}Cmd c, HttpServletRequest r) throws URISyntaxException {
      |    return service.retrieve(c, r.getUserPrincipal(), new URI(r.getRequestURI()));
      |  }
      |  @RequestMapping(value="${cToShell("%s%s%s".format("update", hyphen, cToShell(entityName)))}", produces="application/json")
      |  public void update(@RequestBody Update${cToPascal(entityName)}Cmd c, HttpServletRequest r) throws URISyntaxException {
      |    service.update(c, r.getUserPrincipal(), new URI(r.getRequestURI()));
      |  }
      |
      |  @RequestMapping(value="${cToShell("%s%s%s".format("delete", hyphen, cToShell(entityName)))}", produces="application/json")
      |  public void delete(@RequestBody Delete${cToPascal(entityName)}Cmd c, HttpServletRequest r) throws URISyntaxException {
      |    service.delete(c, r.getUserPrincipal(), new URI(r.getRequestURI()));
      |  }
      |
      |  @RequestMapping(value="${cToShell("%s%s%s".format("query", hyphen, cToShell(entityName)))}", produces="application/json")
      |  public ${cToPascal(entityName)}ListVo query(@RequestBody QueryCommand q, HttpServletRequest r) throws URISyntaxException {
      |    return service.query(q, r.getUserPrincipal(), new URI(r.getRequestURI()));
      |  }
      |
      |  @RequestMapping(value="${cToShell("%s%s%s".format("export", hyphen, cToShell(entityName)))}", consumes="application/json")
      |  public void export(@RequestBody QueryCommand q, HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, IOException {
      |    final ${cToPascal(entityName)}ListVo listVo = service.query(q, request.getUserPrincipal(), new URI(request.getRequestURI()));
      |    HSSFWorkbook wb = new HSSFWorkbook();
      |    HSSFSheet sheet = wb.createSheet("${cToPascal(entityName)}");
      |
      |    HSSFCellStyle style = wb.createCellStyle();
      |    style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.AQUA.getIndex());
      |    style.setFillPattern(FillPatternType.BIG_SPOTS);
      |
      |    style = wb.createCellStyle();
      |    style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.ORANGE.getIndex());
      |    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      |
      |    short rowNumber = 0;
      |    exportHeaderCells(sheet.createRow(rowNumber++), style);
      |    for(${cToPascal(entityName)}Vo vo: listVo.getItemsList()) {
      |      HSSFRow row = sheet.createRow(rowNumber++);
      |      exportDataCells(vo, row, style);
      |    }
      |
      |    response.setContentType("application/vnd.ms-excel");
      |    response.setHeader("Content-disposition",String.format("attachment; filename=%sList-%s.xls", "${cToPascal(entityName)}", formatTimestamp(new Date())));
      |    wb.write(response.getOutputStream());
      |  }
      |
      |  private void exportHeaderCells(HSSFRow row, HSSFCellStyle style) {
      |    short colNumber = 0;
      |    HSSFCell cell = null;
      |    ${indent(headerCells(columns), 4)}
      |  }
      |
      |  private void exportDataCells(${cToPascal(entityName)}Vo vo, HSSFRow row, HSSFCellStyle style) {
      |    short colNumber = 0;
      |    HSSFCell cell = null;
      |    ${indent(dataCells(columns), 4)}
      |  }
      |""".stripMargin

    val end =
      """}
        |""".stripMargin

    val printWriter = new PrintWriter(s"${srcDir}/${cToPascal(entityName)}Controller.java", "utf-8")

    printWriter.print(prelude)
    printWriter.print(end)

    printWriter.close()
  }

  private def headerCells(columns: Seq[(String, String)]): String = columns
    .map(x => String.format("cell = row.createCell(colNumber++);\ncell.setCellValue(\"%s\");\ncell.setCellStyle(style);", cToPascal(x._1)))
    .reduce((x, y) => "%s\n%s".format(x, y))

  private def dataCells(columns: Seq[(String, String)]): String = columns
    .map(x => String.format("cell = row.createCell(colNumber++);\ncell.setCellValue(%s);\ncell.setCellStyle(style);", formatCell(x._1, x._2)))
    .reduce((x, y) => "%s\n%s".format(x, y))

  private def formatCell(name: String, t: String): String =
    if(t.equalsIgnoreCase("timestamp"))
      s"""String.format("%s", formatTimestamp(vo.get${cToPascal(name)}()))"""
    else
      s"""String.format("%s", vo.get${cToPascal(name)}())"""

  private def project = {
    val printWriter = new PrintWriter(s"${projectDir}/pom.xml", "utf-8")

    val source =
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         |         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
         |  <modelVersion>4.0.0</modelVersion>
         |
         |  <groupId>${modelPackage}</groupId>
         |  <artifactId>${cToShell(modelName)}-controller</artifactId>
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
         |      <artifactId>${cToShell(modelName)}-message</artifactId>
         |      <version>1.0-SNAPSHOT</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>${modelPackage}</groupId>
         |      <artifactId>${cToShell(modelName)}-service</artifactId>
         |      <version>1.0-SNAPSHOT</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>com.github.apuex.springbootsolution</groupId>
         |      <artifactId>runtime_2.12</artifactId>
         |      <version>1.0.6</version>
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
         |      <groupId>org.apache.poi</groupId>
         |      <artifactId>poi</artifactId>
         |      <version>3.17</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.apache.poi</groupId>
         |      <artifactId>poi-ooxml</artifactId>
         |      <version>3.17</version>
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
