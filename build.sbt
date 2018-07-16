import Dependencies._

name         := "backend-codegen"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

lazy val root = (project in file("."))
  .aggregate(
    play_akka_codegen,
    spring_boot_codegen,
    json_runtime,
    java_sql_query,
    scala_sql_query
  )

lazy val play_akka_codegen = (project in file("play-akka-codegen")).dependsOn(scala_sql_query)
lazy val spring_boot_codegen = (project in file("spring-boot-codegen")).dependsOn(scala_sql_query)
lazy val scala_sql_query = (project in file("scala-sql-query"))
lazy val java_sql_query = (project in file("java-sql-query"))
lazy val json_runtime = (project in file("json-runtime"))

publishTo := localRepo

