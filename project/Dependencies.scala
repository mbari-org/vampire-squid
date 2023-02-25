import sbt._
object Dependencies {

  lazy val activation = "com.sun.activation"                             % "javax.activation" % "1.2.0"
  lazy val auth0 = "com.auth0" % "java-jwt" % "3.19.2"

  private val circeVersion = "0.14.2"
  lazy val circeCore       = "io.circe" %% "circe-core"    % circeVersion
  lazy val circeGeneric    = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser     = "io.circe" %% "circe-parser"  % circeVersion

  lazy val commonsCodec = "commons-codec"                                  % "commons-codec" % "1.15"
  lazy val gson = "com.google.code.gson"                           % "gson"           % "2.9.1"
  lazy val gsonJavaTime = "com.fatboyindustrial.gson-javatime-serialisers" % "gson-javatime-serialisers" % "1.1.2"
  lazy val h2 = "com.h2database"                                 % "h2"            % "2.1.214"
  lazy val jansi    = "org.fusesource.jansi"         % "jansi"           % "2.4.0"
  lazy val jasypt   = "org.jasypt"                   % "jasypt"          % "1.9.3"
  lazy val javamelody = "net.bull.javamelody"                            % "javamelody-core" % "1.91.0"
  lazy val jta = "javax.transaction"                              % "jta" % "1.1"
  lazy val logback  = "ch.qos.logback"               % "logback-classic" % "1.3.0-alpha16"
  lazy val methanol = "com.github.mizosoft.methanol" % "methanol"        % "1.7.0"
  lazy val microsoft = "com.microsoft.sqlserver"                        % "mssql-jdbc" % "9.4.0.jre11"
  lazy val munit    = "org.scalameta"               %% "munit"           % "1.0.0-M6"
  lazy val oracle = "com.oracle.ojdbc"                               % "ojdbc8" % "19.3.0.0"
  lazy val picocli  = "info.picocli"                 % "picocli"         % "4.6.3"

  lazy val slf4jVersion = "2.0.0-alpha7"
  lazy val slf4jApi     = "org.slf4j" % "slf4j-api"    % slf4jVersion
  lazy val slf4jJul     = "org.slf4j" % "jul-to-slf4j" % slf4jVersion

  private val tapirVersion  = "1.0.3"
  lazy val tapirStubServer  = "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion
  lazy val tapirSwagger     = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion
  lazy val tapirCirce       = "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion
  lazy val tapirCirceClient = "com.softwaremill.sttp.client3" %% "circe" % "3.7.2"
  lazy val tapirVertx       = "com.softwaremill.sttp.tapir" %% "tapir-vertx-server" % tapirVersion

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.4.2"

  private val jaxbVersion = "3.0.1"
  lazy val jaxbCore = "com.sun.xml.bind"                               % "jaxb-core" % jaxbVersion
  lazy val jaxbImpl = "com.sun.xml.bind"                               % "jaxb-impl" % jaxbVersion
  lazy val jaxbApi  = "javax.xml.bind"                                 % "jaxb-api"  % jaxbVersion
}
