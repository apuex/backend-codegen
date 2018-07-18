import Dependencies._

name := "java-sql-query"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  jodaTime,
  slf4jApi % Test,
  slf4jSimple % Test,
  scalaTestPlusPlay % Test
)

PB.targets in Compile := Seq(
  scalapb.gen(javaConversions=false) -> (sourceManaged in Compile).value
)

publishTo := localRepo
