val activationVersion   = "1.2.0"
val akkaVersion         = "2.6.19"
val auth0Version        = "3.19.2"
val circeVersion        = "0.14.3"
val codecVersion        = "1.15"
val configVersion       = "1.4.2"
val derbyVersion        = "10.16.1.1"
val eclipselinkVersion  = "2.7.11"
val gsonJavatimeVersion = "1.1.2"
val gsonVersion         = "2.9.1"
val h2Version           = "2.1.214"
val jansiVersion        = "2.4.0"
val javamelodyVersion   = "1.91.0"
val jettyVersion        = "9.4.48.v20220622"
val json4sVersion       = "4.0.6" //"3.6.11" // Scalatra is not compatible with v4.0.0
val jtaVersion          = "1.1"
val jtdsVersion         = "1.3.1"
val junitVersion        = "4.13.2"
val logbackVersion      = "1.4.0"
val oracleVersion       = "19.3.0.0"
val postgresqlVersion   = "42.5.0"
val rabbitmqVersion     = "5.15.0"
val scalaTestVersion    = "3.2.14"
val scalatraVersion     = "2.8.4"
val servletVersion      = "4.0.1"
val slf4jVersion        = "2.0.0"
val sqlserverVersion    = "9.4.1.jre11"
val xmlBindVersion      = "2.3.0"

Global / onChangedBuildSource := ReloadOnSourceChanges


lazy val buildSettings = Seq(
  organization := "org.mbari.vars",
  scalaVersion := "2.13.8",
  crossScalaVersions := Seq("2.13.8"),
  organizationName := "Monterey Bay Aquarium Research Institute",
  startYear := Some(2017),
  licenses += ("Apache-2.0", new URL(
    "https://www.apache.org/licenses/LICENSE-2.0.txt"
  ))
)

lazy val consoleSettings = Seq(
  shellPrompt := { state =>
    val user = System.getProperty("user.name")
    user + "@" + Project.extract(state).currentRef.project + ":sbt> "
  },
  initialCommands in console :=
    """
      |import java.time.Instant
      |import java.util.UUID
    """.stripMargin
)

lazy val dependencySettings = Seq(
  resolvers ++= Seq(
    Resolver.mavenLocal,
    Resolver.githubPackages("mbari-org", "maven")
  )
)

lazy val optionSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "UTF-8",         // yes, this is 2 args. Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature",      // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xlint",
    "-Yrangepos",              // required by SemanticDB compiler plugin
    "-Ywarn-dead-code",        // Warn when dead code is identified.
    "-Ywarn-extra-implicit",   // Warn when more than one implicit parameter section is defined.
    "-Ywarn-numeric-widen",    // Warn when numerics are widened.
    "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",   // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",    // Warn if a local definition is unused.
    "-Ywarn-unused:params",    // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",   // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",  // Warn if a private member is unused.
    "-Ywarn-value-discard"     // Warn when non-Unit expression results are unused.
  ),
  javacOptions ++= Seq("-target", "17", "-source", "17"),
  updateOptions := updateOptions.value.withCachedResolution(true)
)

lazy val appSettings = buildSettings ++
  consoleSettings ++
  dependencySettings ++
  optionSettings ++ Seq(
  fork := true
)

lazy val apps = Map("jetty-main" -> "JettyMain") // for sbt-pack

lazy val `vampire-squid` = (project in file("."))
  .enablePlugins(
    AutomateHeaderPlugin, 
    GitBranchPrompt, 
    GitVersioning, 
    JettyPlugin,
    PackPlugin)
  .settings(appSettings)
  .settings(
    // Set version based on git tag. I use "0.0.0" format (no leading "v", which is the default)
    // Use `show gitCurrentTags` in sbt to update/see the tags
    git.gitTagToVersionNumber := { tag: String =>
      if(tag matches "[0-9]+\\..*") Some(tag)
      else None
    },
    git.useGitDescribe := true,
    libraryDependencies ++= Seq(
      "ch.qos.logback"                                 % "logback-classic"                   % logbackVersion,
      "ch.qos.logback"                                 % "logback-core"                      % logbackVersion,
      "com.auth0"                                      % "java-jwt"                          % auth0Version,
      "com.fatboyindustrial.gson-javatime-serialisers" % "gson-javatime-serialisers"         % gsonJavatimeVersion,
      "com.google.code.gson"                           % "gson"                              % gsonVersion,
      "com.h2database"                                 % "h2"                                % h2Version % "test",
      "com.rabbitmq"                                   % "amqp-client"                       % rabbitmqVersion,
      "com.oracle.ojdbc"                               % "ojdbc8"                            % oracleVersion,
      "com.sun.activation"                             % "javax.activation"                  % activationVersion,
      "com.sun.xml.bind"                               % "jaxb-core"                         % xmlBindVersion,
      "com.sun.xml.bind"                               % "jaxb-impl"                         % xmlBindVersion,
      "com.microsoft.sqlserver"                        % "mssql-jdbc"                        % sqlserverVersion,
      "com.typesafe"                                   % "config"                            % configVersion,
      "com.typesafe.akka"                              %% "akka-actor"                       % akkaVersion,
      "commons-codec"                                  % "commons-codec"                     % codecVersion,
      "javax.servlet"                                  % "javax.servlet-api"                 % servletVersion,
      "javax.transaction"                              % "jta"                               % jtaVersion,
      "javax.xml.bind"                                 % "jaxb-api"                          % xmlBindVersion,
      "junit"                                          % "junit"                             % junitVersion % "test",
      "io.circe"                                       %% "circe-core"                       % circeVersion,
      "io.circe"                                       %% "circe-generic"                    % circeVersion,
      "io.circe"                                       %% "circe-parser"                     % circeVersion,
      "net.bull.javamelody"                            % "javamelody-core"                   % javamelodyVersion,
      "net.sourceforge.jtds"                           % "jtds"                              % jtdsVersion,
      "org.apache.derby"                               % "derby"                             % derbyVersion, //          % "test",
      "org.apache.derby"                               % "derbyclient"                       % derbyVersion, //          % "test",
      "org.apache.derby"                               % "derbynet"                          % derbyVersion, //          % "test",
      "org.apache.derby"                               % "derbyshared"                       % derbyVersion,
      "org.apache.derby"                               % "derbytools"                        % derbyVersion,
      "org.eclipse.jetty"                              % "jetty-server"                      % jettyVersion % "container;compile;test",
      "org.eclipse.jetty"                              % "jetty-servlets"                    % jettyVersion % "container;compile;test",
      "org.eclipse.jetty"                              % "jetty-webapp"                      % jettyVersion % "container;compile;test",
      "org.eclipse.persistence"                        % "org.eclipse.persistence.extension" % eclipselinkVersion,
      "org.eclipse.persistence"                        % "org.eclipse.persistence.jpa"       % eclipselinkVersion,
      "org.fusesource.jansi"                           % "jansi"                             % jansiVersion % "runtime",
      "org.json4s"                                     %% "json4s-jackson"                   % json4sVersion,
      "org.postgresql"                                 % "postgresql"                        % postgresqlVersion,
      "org.scalatest"                                  %% "scalatest"                        % scalaTestVersion % "test",
      "org.scalatra"                                   %% "scalatra"                         % scalatraVersion,
      "org.scalatra"                                   %% "scalatra-json"                    % scalatraVersion,
      "org.scalatra"                                   %% "scalatra-scalate"                 % scalatraVersion,
      "org.scalatra"                                   %% "scalatra-scalatest"               % scalatraVersion,
      "org.scalatra"                                   %% "scalatra-swagger"                 % scalatraVersion,
      "org.slf4j"                                      % "log4j-over-slf4j"                  % slf4jVersion,
      "org.slf4j"                                      % "slf4j-api"                         % slf4jVersion
    ).map(
      _.excludeAll(
        ExclusionRule("org.slf4j", "slf4j-jdk14"),
        ExclusionRule("javax.servlet", "servlet-api")
      )
    )
  )
  .settings( // config sbt-pack
    packMain := apps,
    packExtraClasspath := apps
      .keys
      .map(k => k -> Seq("${PROG_HOME}/conf"))
      .toMap,
    packJvmOpts := apps
      .keys
      .map(k => k -> Seq("-Duser.timezone=UTC", "-Xmx4g"))
      .toMap,
    packDuplicateJarStrategy := "latest",
    packJarNameConvention := "original"
  )

