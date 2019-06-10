import Dependencies._

name         := "spring-boot-solution"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

lazy val root = (project in file("."))
  .aggregate(
    codegen,
    `java-runtime`,
    `scala-runtime`,
    util,
  )

lazy val codegen = (project in file("codegen"))
    .dependsOn(`java-runtime`)
    .enablePlugins(GraalVMNativeImagePlugin)

lazy val `java-runtime` = (project in file("java-runtime"))
  .dependsOn(util)
  .enablePlugins(ProtobufPlugin)

lazy val `scala-runtime` = (project in file("scala-runtime"))
  .dependsOn(util)

lazy val util = (project in file("util"))

publishTo := sonatypePublishTo.value

