package com.github.apuex.codegen.playakka

object Main extends App {
  if(args.length == 0) {
    println("Usage:\n" +
      "\tjava -jar <this jar> <arg list>")
  } else {
    args(0) match {
      case "dump-schema" => DumpSchema.main(args.drop(1))
      case "generate" => GenerateAll.main(args.drop(1))
      case "generate-all" => GenerateAll.main(args.drop(1))
      case "generate-controller" => Controller.main(args.drop(1))
      case "generate-dao" => Dao.main(args.drop(1))
      case "generate-entity" => Entity.main(args.drop(1))
      case "generate-json" => JsonSupport.main(args.drop(1))
      case "generate-router" => Router.main(args.drop(1))
      case "generate-service" => Service.main(args.drop(1))
      case "generate-transaction" => Transaction.main(args.drop(1))
      case c =>
        println(s"unknown command '${c}'")
    }
  }
}