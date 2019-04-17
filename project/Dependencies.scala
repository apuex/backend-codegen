import sbt._

object Dependencies {
  lazy val scalaVersionNumber    = "2.12.8"
  lazy val akkaVersion           = "2.5.22"
  lazy val artifactVersionNumber = "1.0.7"
  lazy val artifactGroupName     = "com.github.apuex.springbootsolution"
  lazy val sprayVersion          = "1.3.3"
  lazy val playVersion           = "2.6.9"
  lazy val playSilhouetteVersion = "5.0.3"

  lazy val scalaXml         = "org.scala-lang.modules"    %%  "scala-xml"                          % "1.0.6"
  lazy val cassandraDriver  = "com.datastax.cassandra"    %   "cassandra-driver-core"              % "3.6.0"
  lazy val protobufJava     = "com.google.protobuf"       %   "protobuf-java"                      % "3.6.1"
  lazy val protobufJavaUtil = "com.google.protobuf"       %   "protobuf-java-util"                 % "3.6.1"
  lazy val jodaTime         = "joda-time"                 %   "joda-time"                          % "2.9.9"
  lazy val mysqlDriver      = "mysql"                     %   "mysql-connector-java"               % "6.0.6"
  lazy val mssqlDriver      = "com.microsoft.sqlserver"   %   "mssql-jdbc"                         % "7.0.0.jre8"
  lazy val oracleDriver     = "com.oracle"                %   "ojdbc6"                             % "12.1.0.1-atlassian-hosted"
  lazy val jgraphtCore      = "org.jgrapht"               %   "jgrapht-core"                       % "1.1.0"

  lazy val slf4jApi         = "org.slf4j"                 %   "slf4j-api"                          % "1.7.25"
  lazy val slf4jSimple      = "org.slf4j"                 %   "slf4j-simple"                       % "1.7.25"
  lazy val scalaTest        = "org.scalatest"             %%  "scalatest"                          % "3.0.4"

  lazy val confPath = "../conf"

  lazy val localRepo = Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
}
