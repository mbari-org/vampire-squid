import sbt._
object Dependencies {


  val circeVersion          = "0.14.5"
  lazy val circeCore        = "io.circe"                %% "circe-core"         % circeVersion
  lazy val circeGeneric     = "io.circe"                %% "circe-generic"      % circeVersion
  lazy val circeParser      = "io.circe"                %% "circe-parser"       % circeVersion

  lazy val commonsCodec     = "commons-codec"           % "commons-codec"       % "1.15"

  val derbyVersion          = "10.16.1.1"
  lazy val derby            = "org.apache.derby"        % "derby"               % derbyVersion //          % "test"
  lazy val derbyClient      = "org.apache.derby"        % "derbyclient"         % derbyVersion //          % "test"
  lazy val derbyNet         = "org.apache.derby"        % "derbynet"            % derbyVersion //          % "test"
  lazy val derbyShared      = "org.apache.derby"        % "derbyshared"         % derbyVersion
  lazy val derbyTools       = "org.apache.derby"        % "derbytools"          % derbyVersion

  // val eclipsePersistenceVersion        = "2.7.12"
  val eclipsePersistenceVersion        = "4.0.1"
  lazy val eclipsePersistenceExtension = "org.eclipse.persistence" % "org.eclipse.persistence.extension" % eclipsePersistenceVersion
  lazy val eclipsePersistenceJpa       = "org.eclipse.persistence" % "org.eclipse.persistence.jpa" % eclipsePersistenceVersion

  lazy val gson             = "com.google.code.gson"    % "gson"                % "2.10.1"
  lazy val gsonJavatime     = "com.fatboyindustrial.gson-javatime-serialisers"  % "gson-javatime-serialisers" % "1.1.2"
  lazy val h2               = "com.h2database"          % "h2"                  % "2.1.214"
  lazy val jansi            = "org.fusesource.jansi"    % "jansi"               % "2.4.0"
  lazy val javaJwt          = "com.auth0"               % "java-jwt"            % "4.4.0"
  lazy val javamelodyCore   = "net.bull.javamelody"     % "javamelody-core"     % "1.94.0"
  lazy val javaxActivation  = "com.sun.activation"      % "javax.activation"    % "1.2.0"
  lazy val javaxJta         = "javax.transaction"       % "jta"                 % "1.1"
  lazy val javaxServlet     = "javax.servlet"           % "javax.servlet-api"   % "4.0.1"

  val xmlBindVersion        = "4.0.2"
  // val xmlBindVersion        = "2.3.0"
  // lazy val jaxbApi          = "javax.xml.bind"          % "jaxb-api"            % xmlBindVersion
  lazy val jaxbCore         = "com.sun.xml.bind"        % "jaxb-core"           % xmlBindVersion
  lazy val jaxbImpl         = "com.sun.xml.bind"        % "jaxb-impl"           % xmlBindVersion

  val jettyVersion          = "9.4.51.v20230217"
  lazy val jettyServer      = "org.eclipse.jetty"       % "jetty-server"        % jettyVersion 
  lazy val jettyServlets    = "org.eclipse.jetty"       % "jetty-servlets"      % jettyVersion
  lazy val jettyWebapp      = "org.eclipse.jetty"       % "jetty-webapp"        % jettyVersion

  lazy val json4sJackson    = "org.json4s"              %% "json4s-jackson"     % "4.0.6"
  lazy val jtds             = "net.sourceforge.jtds"    % "jtds"                % "1.3.1"
  lazy val junit            = "junit"                   % "junit"               % "4.13.2"

  val logbackVersion        = "1.4.7"
  lazy val logbackClassic   = "ch.qos.logback"          % "logback-classic"     % logbackVersion
  lazy val logbackCore      = "ch.qos.logback"          % "logback-core"        % logbackVersion

  lazy val mssqlJdbc        = "com.microsoft.sqlserver" % "mssql-jdbc"          % "9.4.1.jre11"
  lazy val oracleJdbc       = "com.oracle.ojdbc"        % "ojdbc8"              % "19.3.0.0"
  lazy val postgresql       = "org.postgresql"          % "postgresql"          % "42.6.0"
  lazy val scalatest        = "org.scalatest"           %% "scalatest"          % "3.2.16" 

  val scalatraVersion       = "3.0.0-M3"
  lazy val scalatra         = "org.scalatra"            %% "scalatra"           % scalatraVersion
  lazy val scalatraJson     = "org.scalatra"            %% "scalatra-json"      % scalatraVersion
  lazy val scalatraTest     = "org.scalatra"            %% "scalatra-scalatest" % scalatraVersion

  val slf4jVersion          = "2.0.7"
  lazy val slf4jApi         = "org.slf4j"               % "slf4j-api"           % slf4jVersion
  lazy val slf4jLog4j       = "org.slf4j"               % "log4j-over-slf4j"    % slf4jVersion

  lazy val typesafeConfig   = "com.typesafe"            % "config"              % "1.4.2"

}
