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
    sql_runtime
  )

lazy val play_akka_codegen = (project in file("play-akka-codegen")).dependsOn(sql_runtime)
lazy val spring_boot_codegen = (project in file("spring-boot-codegen")).dependsOn(sql_runtime)
lazy val sql_runtime = (project in file("sql-runtime"))
lazy val json_runtime = (project in file("json-runtime"))

publishTo := localRepo

