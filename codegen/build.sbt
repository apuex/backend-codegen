import Dependencies._
import sbtassembly.MergeStrategy

name := "codegen"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  scalaXml,
  jtdsDriver,
  slf4jApi % Test,
  slf4jSimple % Test,
  scalaTest % Test,
  scalaTesplusPlay % Test
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.rename
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("com.github.apuex.springbootsolution.codegen.Main")
assemblyJarName in assembly := s"${artifactGroupName}.${name.value}.jar"

publishTo := sonatypePublishTo.value
