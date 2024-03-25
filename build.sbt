import java.net.URI
import Dependencies.*

val tapirVersion          = "1.9.8"
val eclipselinkVersion    = "4.0.2"
val derbyVersion          = "10.16.1.1"
val testcontainersVersion = "0.41.0"

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / licenses         := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / scalaVersion     := "3.3.3"
// ThisBuild / version          := "0.0.1"
ThisBuild / organization     := "org.mbari"
ThisBuild / organizationName := "Monterey Bay Aquarium Research Institute"
ThisBuild / startYear        := Some(2021)
ThisBuild / versionScheme    := Some("semver-spec")

ThisBuild / Test / fork              := true
ThisBuild / Test / parallelExecution := false
ThisBuild / Test / testOptions += Tests.Argument(TestFrameworks.MUnit, "-b")
ThisBuild / Test / javaOptions ++= Seq(
    "-Duser.timeszone=UTC"
)



lazy val vampireSquid = (project in file("vampire-squid"))
    .enablePlugins(
        AutomateHeaderPlugin,
        GitBranchPrompt,
        GitVersioning,
        JavaAppPackaging
    )
    .settings(
        Seq(
            name                      := "vampire-squid",
            // https://stackoverflow.com/questions/22772812/using-sbt-native-packager-how-can-i-simply-prepend-a-directory-to-my-bash-scrip
            bashScriptExtraDefines ++= Seq(
                """addJava "-Dconfig.file=${app_home}/../conf/application.conf"""",
                """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml"""",
                """addJava "-Djava.util.logging.config.file=${app_home}/../conf/logging.properties""""
            ),
            batScriptExtraDefines ++= Seq(
                """call :add_java "-Dconfig.file=%APP_HOME%\conf\application.conf"""",
                """call :add_java "-Dlogback.configurationFile=%APP_HOME%\conf\logback.xml"""",
                """call :add_java "-Djava.util.logging.config.file=%APP_HOME%\conf\logging.properties""""
            ),
            git.gitTagToVersionNumber := {
                tag: String =>
                    if (tag matches "[0-9]+\\..*") Some(tag) else None
            },
            git.useGitDescribe        := true,
            javacOptions ++= Seq("-target", "17", "-source", "17"),
            // licenses += ("Apache-2.0", URI.create("https://www.apache.org/licenses/LICENSE-2.0.txt").toURL),
            libraryDependencies ++= Seq(
                derby,
                derbyClient,
                derbyNet,
                derbyShared,
                derbyTools,
                hibernateCore,
                hibernateEnvers,
                hibernateHikari,
                javaJwt,
                logbackClassic,
                slf4jSystem,
                mssqlJdbc,
                munit           % Test,
                oracleJdbc,
                postgresql,
                scalatest       % Test,
                tapirCirce,
                tapirPrometheus,
                tapirServerStub % Test,
                tapirSttpCirce,
                tapirSwagger,
                tapirVertex,
                typesafeConfig
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

lazy val integrationTests = (project in file("it"))
    .dependsOn(vampireSquid)
    .enablePlugins(
        AutomateHeaderPlugin,
    )
    .settings(
        libraryDependencies ++= Seq(
            derby,
            derbyClient,
            derbyNet,
            derbyShared,
            derbyTools,
            munit,
            scalatest,
            tapirServerStub,
            testcontainersCore
        ),
        // licenses += ("Apache-2.0", URI.create("https://www.apache.org/licenses/LICENSE-2.0.txt").toURL)
    )

// lazy val itOracle = (project in file("it-oracle"))
//   .dependsOn(integrationTests)
//   .settings(
//     libraryDependencies ++= Seq(
//         testcontainersOracle
//     )
//   )

lazy val itPostgres = (project in file("it-postgres"))
    .dependsOn(integrationTests)
    .enablePlugins(
        AutomateHeaderPlugin,
    )
    .settings(
        libraryDependencies ++= Seq(
            testcontainersPostgres
        ),
        // licenses += ("Apache-2.0", URI.create("https://www.apache.org/licenses/LICENSE-2.0.txt").toURL),
    )

lazy val itSqlserver = (project in file("it-sqlserver"))
    .dependsOn(integrationTests)
    .enablePlugins(
        AutomateHeaderPlugin,
    )
    .settings(
        libraryDependencies ++= Seq(
            testcontainersSqlserver
        ),
        // licenses += ("Apache-2.0", URI.create("https://www.apache.org/licenses/LICENSE-2.0.txt").toURL),
    )
