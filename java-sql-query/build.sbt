import Dependencies._

name := "java-sql-query"
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

publishTo := localRepo
