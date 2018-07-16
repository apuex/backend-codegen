import Dependencies._

name := "json-runtime"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  playJson,
  googleGuice,
  jodaTime,
  scalapbCompiler,
  scalapbRuntime % "protobuf",
  slf4jApi % Test,
  slf4jSimple % Test,
  scalaTestPlusPlay % Test
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

publishTo := localRepo
