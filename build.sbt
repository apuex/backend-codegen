import Dependencies._

name         := "backend-codegen"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

lazy val root = (project in file("."))
  .aggregate(
    spring_boot_codegen,
    sql_runtime
  )

lazy val spring_boot_codegen = (project in file("spring-boot-codegen")).dependsOn(sql_runtime)
lazy val sql_runtime = (project in file("sql-runtime"))

publishTo := localRepo

