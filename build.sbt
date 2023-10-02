import java.net.URI

val tapirVersion       = "1.7.5"
val eclipselinkVersion = "4.0.2"
val derbyVersion       = "10.16.1.1"

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "3.3.1"
// ThisBuild / version          := "0.0.1"
ThisBuild / organization     := "org.mbari"
ThisBuild / organizationName := "MBARI"
ThisBuild / startYear        := Some(2021)
ThisBuild / versionScheme    := Some("semver-spec")
Test / parallelExecution     := false

lazy val rootProject = (project in file("."))
  .enablePlugins(
    AutomateHeaderPlugin,
    GitBranchPrompt,
    GitVersioning,
    JavaAppPackaging,
  )
  .settings(
    Seq(
      name                      := "vampire-squid",
      git.gitTagToVersionNumber := {
        tag: String =>
          if (tag matches "[0-9]+\\..*") Some(tag)
          else None
      },
      git.useGitDescribe        := true,
      javacOptions ++= Seq("-target", "17", "-source", "17"),
      licenses += ("Apache-2.0", URI.create("https://www.apache.org/licenses/LICENSE-2.0.txt").toURL),
      libraryDependencies ++= Seq(
        "ch.qos.logback"                 % "logback-classic"                   % "1.4.11",
        "com.auth0" % "java-jwt" % "4.4.0",
        "com.softwaremill.sttp.client3" %% "circe"                             % "3.9.0"      % Test,
        "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"                  % tapirVersion,
        "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics"          % tapirVersion,
        "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"            % tapirVersion % Test,
        "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"           % tapirVersion,
        "com.softwaremill.sttp.tapir"   %% "tapir-vertx-server"                % tapirVersion,
        "org.apache.derby"               % "derby"                             % derbyVersion % "test",
        "org.apache.derby"               % "derbyclient"                       % derbyVersion % "test",
        "org.apache.derby"               % "derbynet"                          % derbyVersion % "test",
        "org.apache.derby"               % "derbyshared"                       % derbyVersion,
        "org.apache.derby"               % "derbytools"                        % derbyVersion,
        "org.scalameta"                 %% "munit"                             % "1.0.0-M10"  % Test,
        "org.hibernate.orm"              % "hibernate-core"                    % "6.3.0.Final",
        "org.hibernate.orm"              % "hibernate-hikaricp"                    % "6.3.0.Final",
        // "org.eclipse.persistence"        % "eclipselink"                       % eclipselinkVersion,
        // "org.eclipse.persistence"        % "org.eclipse.persistence.extension" % eclipselinkVersion,
        "org.scalatest"                 %% "scalatest"                         % "3.2.17"     % Test,
        "com.typesafe"                   % "config"                            % "1.4.2"
      ),
      scalacOptions ++= Seq(
        "-deprecation", // Emit warning and location for usages of deprecated APIs.
        "-encoding",
        "UTF-8",        // yes, this is 2 args. Specify character encoding used by source files.
        "-explain",     // Explain errors in more detail.
        "-feature",     // Emit warning and location for usages of features that should be imported explicitly.
        "-language:existentials",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-language:postfixOps",
        "-indent",
        "-rewrite",
        "-unchecked",
        "-Vprofile"
      )
    )
  )

// https://stackoverflow.com/questions/22772812/using-sbt-native-packager-how-can-i-simply-prepend-a-directory-to-my-bash-scrip
bashScriptExtraDefines ++= Seq(
  """addJava "-Dconfig.file=${app_home}/../conf/application.conf"""",
  """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml"""",
  """addJava "-Djava.util.logging.config.file=${app_home}/../conf/logging.properties""""
)
batScriptExtraDefines ++= Seq(
  """call :add_java "-Dconfig.file=%APP_HOME%\conf\application.conf"""",
  """call :add_java "-Dlogback.configurationFile=%APP_HOME%\conf\logback.xml"""",
  """call :add_java "-Djava.util.logging.config.file=%APP_HOME%\conf\logging.properties""""
)
