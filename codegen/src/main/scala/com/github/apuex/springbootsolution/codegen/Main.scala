package com.github.apuex.springbootsolution.codegen

object Main extends App {
  if(args.length == 0) {
    println("Usage:\n" +
      "\tjava -jar <this jar> <arg list>")
  } else {
    args(0) match {
      case "dump-schema" => DumpSchema.main(args.drop(1))
      case "dump-oracle-schema" => DumpOracleSchema.main(args.drop(1))
      case "filter-entity" => FilterEntity.main(args.drop(1))
      case "generate" => GenerateAll.main(args.drop(1))
      case "generate-all" => GenerateAll.main(args.drop(1))
      case "generate-app" => App.main(args.drop(1))
      case "generate-controller" => Controller.main(args.drop(1))
      case "generate-api-list" => ApiList.main(args.drop(1))
      case "generate-sqlserver-dao" => DaoSqlServer.main(args.drop(1))
      case "generate-mysql-dao" => DaoMysql.main(args.drop(1))
      case "generate-cassandra-dao" => CassandraDao.main(args.drop(1))
      case "generate-sqlserver-ddl" => SqlServerSchemaGenerator.main(args.drop(1))
      case "generate-mysql-ddl" => MysqlSchemaGenerator.main(args.drop(1))
      case "generate-message" => Message.main(args.drop(1))
      case "generate-project" => Project.main(args.drop(1))
      case "generate-service" => Service.main(args.drop(1))
      case "generate-integration" => Integration.main(args.drop(1))
      case c =>
        println(s"unknown command '${c}'")
    }
  }
}
