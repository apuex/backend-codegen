import Dependencies._

name         := "spring-boot-solution"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

lazy val root = (project in file("."))
  .aggregate(
    codegen,
    runtime,
  )

lazy val codegen = (project in file("codegen")).dependsOn(runtime)
lazy val runtime = (project in file("runtime"))

publishTo := localRepo

