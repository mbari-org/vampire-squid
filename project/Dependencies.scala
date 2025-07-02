import sbt._
object Dependencies {

  val circeVersion      = "0.14.14"
  lazy val circeCore    = "io.circe" %% "circe-core"    % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser  = "io.circe" %% "circe-parser"  % circeVersion

  val derbyVersion     = "10.17.1.0"
  lazy val derby       = "org.apache.derby" % "derby"       % derbyVersion //          % "test"
  lazy val derbyClient = "org.apache.derby" % "derbyclient" % derbyVersion //          % "test"
  lazy val derbyNet    = "org.apache.derby" % "derbynet"    % derbyVersion //          % "test"
  lazy val derbyShared = "org.apache.derby" % "derbyshared" % derbyVersion
  lazy val derbyTools  = "org.apache.derby" % "derbytools"  % derbyVersion

  // val hibernateVersion     = "6.6.17.Final"
  val hibernateVersion     = "7.0.4.Final"
  lazy val hibernateCore   = "org.hibernate.orm" % "hibernate-core"     % hibernateVersion
  lazy val hibernateEnvers = "org.hibernate.orm" % "hibernate-envers"   % hibernateVersion
  lazy val hibernateHikari = "org.hibernate.orm" % "hibernate-hikaricp" % hibernateVersion

  lazy val jansi   = "org.fusesource.jansi" % "jansi"    % "2.4.0"
  lazy val javaJwt = "com.auth0"            % "java-jwt" % "4.5.0"

  val logbackVersion      = "1.5.18"
  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
  lazy val logbackCore    = "ch.qos.logback" % "logback-core"    % logbackVersion

  lazy val mssqlJdbc  = "com.microsoft.sqlserver" % "mssql-jdbc" % "12.10.1.jre11"
  lazy val munit      = "org.scalameta"          %% "munit"      % "1.1.1"
  lazy val oracleJdbc = "com.oracle.ojdbc"        % "ojdbc8"     % "19.3.0.0"
  lazy val postgresql = "org.postgresql"          % "postgresql" % "42.7.7"
  lazy val scalatest  = "org.scalatest"          %% "scalatest"  % "3.2.19"

  val slf4jVersion     = "2.0.17"
  lazy val slf4jApi    = "org.slf4j" % "slf4j-api"                  % slf4jVersion
  lazy val slf4jLog4j  = "org.slf4j" % "log4j-over-slf4j"           % slf4jVersion
  lazy val slf4jSystem = "org.slf4j" % "slf4j-jdk-platform-logging" % slf4jVersion

  private val tapirVersion = "1.11.35"
  lazy val tapirSttpCirce  = "com.softwaremill.sttp.client3" %% "circe"                    % "3.11.0"
  lazy val tapirCirce      = "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"         % tapirVersion
  lazy val tapirPrometheus = "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics" % tapirVersion
  lazy val tapirServerStub = "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"   % tapirVersion
  lazy val tapirSwagger    = "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"  % tapirVersion
  lazy val tapirVertex     = "com.softwaremill.sttp.tapir"   %% "tapir-vertx-server"       % tapirVersion

  val testcontainersVersion        = "1.21.3"
  lazy val testcontainersCore      = "org.testcontainers" % "testcontainers" % testcontainersVersion
  lazy val testcontainersSqlserver = "org.testcontainers" % "mssqlserver"    % testcontainersVersion
  lazy val testcontainersOracle    = "org.testcontainers" % "oracle-xe"      % testcontainersVersion
  lazy val testcontainersPostgres  = "org.testcontainers" % "postgresql"     % testcontainersVersion

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.4.3"

}
