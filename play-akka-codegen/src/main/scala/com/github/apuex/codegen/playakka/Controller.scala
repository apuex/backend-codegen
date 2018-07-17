package com.github.apuex.codegen.playakka

import com.github.apuex.codegen.runtime.SymbolConverters._

import scala.xml._

object Controller extends App {

  val xml = ModelLoader(args(0)).xml
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data

  xml.child.filter(x => x.label == "entity")
    .foreach(x => controllerForEntity(modelPackage, x))

  private def controllerForEntity(modelPackage: String, entity: Node) = {
    val entityName = entity.attribute("name").asInstanceOf[Some[Text]].get.data
    val prelude =
    s"""
      |package ${modelPackage}.controller
      |
      |import javax.inject._
      |
      |import akka.actor._
      |import akka.pattern._
      |import akka.util._
      |import ${modelPackage}.json._
      |import ${modelPackage}.message.ResponseType._
      |import ${modelPackage}.message._
      |import play.api.libs.json._
      |import play.api.mvc._
      |
      |import scala.concurrent._
      |import scala.concurrent.duration._
      |
      |@Singleton
      |class ${entityName}Controller @Inject()(cc: ControllerComponents, @Named("${entityName}Service") ${pascalToCamel(entityName)}: ActorRef)(implicit ec: ExecutionContext)
      |  extends AbstractController(cc)
      |    with ${entityName}JsonSupport {
      |  implicit val timeout: Timeout = FiniteDuration(20, SECONDS)
      |
      |  def create = Action.async(parse.json) { request =>
      |    val result = request.body.validate[Create${entityName}Cmd]
      |    result.fold(
      |      errors => Future({
      |        BadRequest(Json.toJson(ResponseVo(BAD_COMMAND, Some(errors.toString()))))
      |      }),
      |      x => {
      |        ${pascalToCamel(entityName)}.ask(x).mapTo[ValueObject].map {
      |          case r@ResponseVo(SUCCESS, _) => Created(Json.toJson(r))
      |          case ResponseVo(ALREADY_EXISTS, _) => NotModified
      |          case r: ResponseVo => InternalServerError(Json.toJson(r))
      |        }
      |      }
      |    )
      |  }
      |
      |  def retrieve = Action.async(parse.json) { request =>
      |    val result = request.body.validate[Retrieve${entityName}Cmd]
      |    result.fold(
      |      errors => Future({
      |        BadRequest(Json.toJson(ResponseVo(BAD_COMMAND, Some(errors.toString()))))
      |      }),
      |      x => {
      |        ${pascalToCamel(entityName)}.ask(x).mapTo[ValueObject].map {
      |          case r: ${entityName}Vo => Ok(Json.toJson(r))
      |          case r@ResponseVo(NOT_EXIST, _) => NotFound(Json.toJson(r))
      |          case r: ResponseVo => InternalServerError(Json.toJson(r))
      |        }
      |      }
      |    )
      |  }
      |
      |  def update = Action.async(parse.json) { request =>
      |    val result = request.body.validate[Update${entityName}Cmd]
      |    result.fold(
      |      errors => Future({
      |        BadRequest(Json.toJson(ResponseVo(BAD_COMMAND, Some(errors.toString()))))
      |      }),
      |      x => {
      |        ${pascalToCamel(entityName)}.ask(x).mapTo[ValueObject].map {
      |          case r@ResponseVo(SUCCESS, _) => Ok(Json.toJson(r))
      |          case r@ResponseVo(NOT_EXIST, _) => NotFound(Json.toJson(r))
      |          case r: ResponseVo => InternalServerError(Json.toJson(r))
      |        }
      |      }
      |    )
      |  }
      |
      |  def query = Action.async(parse.json) { request =>
      |    val result = request.body.validate[QueryCommand]
      |    result.fold(
      |      errors => Future({
      |        BadRequest(Json.toJson(ResponseVo(BAD_COMMAND, Some(errors.toString()))))
      |      }),
      |      x => {
      |        ${pascalToCamel(entityName)}.ask(x).mapTo[ValueObject].map {
      |          case ${entityName}ListVo(list) => Ok(Json.toJson(list))
      |          case r@ResponseVo(NOT_EXIST, _) => NotFound(Json.toJson(r))
      |          case r: ResponseVo => InternalServerError(Json.toJson(r))
      |        }
      |      }
      |    )
      |  }
      |
      |  def delete = Action.async(parse.json) { request =>
      |    val result = request.body.validate[Delete${entityName}Cmd]
      |    result.fold(
      |      errors => Future({
      |        BadRequest(Json.toJson(ResponseVo(BAD_COMMAND, Some(errors.toString()))))
      |      }),
      |      x => {
      |        ${pascalToCamel(entityName)}.ask(x).mapTo[ValueObject].map {
      |          case r@ResponseVo(SUCCESS, _) => Ok(Json.toJson(r))
      |          case r@ResponseVo(NOT_EXIST, _) => NotFound(Json.toJson(r))
      |          case r: ResponseVo => InternalServerError(Json.toJson(r))
      |        }
      |      }
      |    )
      |  }
    """.stripMargin

    val end =
      """
        |}
        |""".stripMargin

    print(prelude)
    print(end)
  }

}
