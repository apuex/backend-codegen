import sbt._

object Dependencies {
  lazy val scalaVersionNumber    = "2.12.6"
  lazy val akkaVersion           = "2.5.11"
  lazy val artifactVersionNumber = "1.0.6"
  lazy val artifactGroupName     = "com.github.apuex.springbootsolution"
  lazy val sprayVersion          = "1.3.3"
  lazy val playVersion           = "2.6.9"
  lazy val playSilhouetteVersion = "5.0.3"

  lazy val scalaXml        = "org.scala-lang.modules"    %%  "scala-xml"                          % "1.0.6"
  lazy val cassandraDriver = "com.datastax.cassandra"    % "cassandra-driver-core"                 % "3.6.0"
  lazy val protobufJava    = "com.google.protobuf"       % "protobuf-java"                         % "3.6.1"
  lazy val protobufJavaUtil= "com.google.protobuf"       % "protobuf-java-util"                    % "3.6.1"
  lazy val jodaTime        = "joda-time"                 %   "joda-time"                           % "2.9.9"
  lazy val mysqlDriver     = "mysql"                     %   "mysql-connector-java"                % "6.0.6"
  lazy val jtdsDriver      = "net.sourceforge.jtds"      %    "jtds"                               % "1.3.1"

  lazy val slf4jApi        = "org.slf4j"                 %  "slf4j-api"                            % "1.7.25"
  lazy val slf4jSimple     = "org.slf4j"                 %  "slf4j-simple"                         % "1.7.25"
  lazy val scalaTest       = "org.scalatest"             %% "scalatest"                            % "3.0.4"

  lazy val confPath = "../conf"

  lazy val localRepo = Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
}
