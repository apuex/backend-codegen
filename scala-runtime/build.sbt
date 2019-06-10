import Dependencies._

name := "scala-runtime"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  scalapbRuntime % "protobuf",
  scalapbJson4s,
  slf4jApi % Test,
  slf4jSimple % Test,
  scalaTest      % Test
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

publishTo := sonatypePublishTo.value
