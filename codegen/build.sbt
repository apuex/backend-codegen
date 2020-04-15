import Dependencies._
import sbtassembly.MergeStrategy

name := "codegen"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

resolvers += "Spring Plugins Repository" at "http://repo.spring.io/plugins-release/"

libraryDependencies ++= Seq(
  scalaXml,
  mssqlDriver,
  mysqlDriver,
  slf4jApi % Test,
  slf4jSimple % Test,
  scalaTest % Test
)

graalVMNativeImageOptions ++= Seq(
  "-H:+ReportUnsupportedElementsAtRuntime",
  "-H:IncludeResources=.*conf",
  "-H:IncludeResources=.*\\.properties",
  "-H:IncludeResources=.*\\.xml",
  "-H:IncludeResourceBundles=com.sun.org.apache.xerces.internal.impl.msg.XMLMessages",
  "-H:ReflectionConfigurationFiles=" + baseDirectory.value / "graal" / "reflection-xml.json"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.rename
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("com.github.apuex.springbootsolution.codegen.Main")
mainClass in Compile := Some("com.github.apuex.springbootsolution.codegen.Main")
assemblyJarName in assembly := s"${name.value}.jar"

publishTo := sonatypePublishTo.value
