import Dependencies._

name := "sql-runtime"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  scalapbCompiler,
  scalapbRuntime % "protobuf",
  slf4jApi % Test,
  slf4jSimple % Test,
  scalaTestPlusPlay % Test
)

PB.targets in Compile := Seq(
  scalapb.gen(javaConversions=false) -> (sourceManaged in Compile).value
)

publishTo := localRepo
