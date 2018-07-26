import Dependencies._

name := "runtime"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  protobufJava,
  protobufJavaUtil,
  jodaTime,
  slf4jApi % Test,
  slf4jSimple % Test,
  scalaTestPlusPlay % Test
)

publishTo := sonatypePublishTo.value
