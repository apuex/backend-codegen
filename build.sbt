import Dependencies._

name         := "spring-boot-solution"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

lazy val root = (project in file("."))
  .aggregate(
    codegen,
    runtime,
    `scala-runtime`,
    util,
  )

lazy val codegen = (project in file("codegen"))
    .dependsOn(runtime)
    .enablePlugins(GraalVMNativeImagePlugin)

lazy val runtime = (project in file("runtime"))
  .dependsOn(util)
  .enablePlugins(ProtobufPlugin)

lazy val `scala-runtime` = (project in file("scala-runtime"))
  .dependsOn(util)

lazy val util = (project in file("util"))

publishTo := sonatypePublishTo.value

