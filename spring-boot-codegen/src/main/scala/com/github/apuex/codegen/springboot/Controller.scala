package com.github.apuex.codegen.springboot

import java.io.{File, PrintWriter}

import com.github.apuex.codegen.runtime.SymbolConverter._

import scala.xml._

object Controller extends App {

  val xml = ModelLoader(args(0)).xml
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val srcDir = s"controller/src/main/java/${modelPackage.replace('.', '/')}/controller"

  new File(srcDir).mkdirs()

  xml.child.filter(x => x.label == "entity")
    .foreach(x => controllerForEntity(modelPackage, x))

  private def controllerForEntity(modelPackage: String, entity: Node) = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val prelude =
    s"""
      |package ${modelPackage}.controller;
      |
      |import ${modelPackage}.message.*;
      |import java.util.*;
      |import org.springframework.web.bind.annotation.*;
      |
      |@RestController
      |@RequestMapping(value="${pascalToShell(entityName)}", method=RequestMethod.POST)
      |public class ${entityName}Controller {
      |  @RequestMapping(value="create-${pascalToShell(entityName)}")
      |  public void create(@RequestBody Create${entityName}Cmd c) {
      |
      |  }
      |
      |  @RequestMapping(value="update-${pascalToShell(entityName)}")
      |  public void update(@RequestBody Update${entityName}Cmd c) {
      |
      |  }
      |
      |  @RequestMapping(value="delete-${pascalToShell(entityName)}")
      |  public void delete(@RequestBody Delete${entityName}Cmd c) {
      |
      |  }
      |
      |  @RequestMapping(value="query-${pascalToShell(entityName)}")
      |  public List<${entityName}> query(QueryCommand q) {
      |    return new ArrayList<>();
      |  }
      |
    """.stripMargin

    val end =
      """
        |}
        |""".stripMargin

    val printWriter = new PrintWriter(s"${srcDir}/${entityName}Controller.java", "utf-8")

    printWriter.print(prelude)
    printWriter.print(end)

    printWriter.close()
  }

}
