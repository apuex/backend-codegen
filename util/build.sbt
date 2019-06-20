import Dependencies._

name := "util"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  protobufJavaUtil,
  scalapbRuntime % "protobuf",
  scalapbJson4s,
  jodaTime,
  slf4jApi % Test,
  slf4jSimple % Test,
  scalaTest % Test
)

publishTo := sonatypePublishTo.value
