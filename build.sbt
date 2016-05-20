// PROJECT PROPERTIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

lazy val baseSettings = Seq(
  organization := "org.mbari.vars" ,
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  javaOptions ++= Seq("-target", "1.8", "-source","1.8"),
  incOptions := incOptions.value.withNameHashing(true),
  resolvers  ++= Seq(Resolver.mavenLocal,
      "hohonuuli-bintray" at "http://dl.bintray.com/hohonuuli/maven"),
  todosTags := Set("TODO", "FIXME", "WTF?"),
  initialCommands in console :=
      """
        |import java.time.{Duration, Instant}
      """.stripMargin,
  scalacOptions  ++= Seq(
          "-deprecation",
          "-encoding", "UTF-8",       // yes, this is 2 args
          "-feature",
          "-language:existentials",
          "-language:higherKinds",
          "-language:implicitConversions",
          "-unchecked",
          "-Xfatal-warnings",
          "-Xlint",
          "-Yno-adapted-args",
          "-Ywarn-numeric-widen",
          "-Xfuture")
)

val configVersion = "1.3.0"
val derbyVersion = "10.12.1.1"
val eclipselinkVersion = "2.6.2"
val gsonJavatimeVersion = "1.1.1"
val gsonVersion = "2.6.2"
val h2Version = "1.4.191"
val jtaVersion = "1.1"
val jtdsVersio = "1.3.1"
val junitVersion = "4.12"
val logbackVersion = "1.1.7"
val scalaTestVersion = "2.2.6"
val slf4jVersion = "1.7.21"

// https://tpolecat.github.io/2014/04/11/scalac-flags.html
scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Xfuture")

lazy val vamModelSettings = baseSettings ++ Seq(
  libraryDependencies ++= {
    Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "ch.qos.logback" % "logback-core" % logbackVersion,
      "com.fatboyindustrial.gson-javatime-serialisers" % "gson-javatime-serialisers" % gsonJavatimeVersion,
      "com.google.code.gson" % "gson" % gsonVersion,
      "com.h2database" % "h2" % h2Version % "test",
      "com.typesafe" % "config" % configVersion,
      "javax.transaction" % "jta" % jtaVersion,
      "junit" % "junit" % junitVersion % "test",
      "net.sourceforge.jtds" % "jtds" % jtdsVersio,
      "org.apache.derby" % "derby" % derbyVersion % "test",
      "org.apache.derby" % "derbyclient" % derbyVersion % "test",
      "org.apache.derby" % "derbynet" % derbyVersion % "test",
      "org.eclipse.persistence" % "eclipselink" % eclipselinkVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion)
  },
  javaOptions in test ++= Seq("-Djava.security.manager","-Djava.security.policy=/Users/brian/workspace/vars2/video-asset-manager/derby.policy"),
  updateOptions := updateOptions.value.withCachedResolution(true)
)

lazy val `vam-model` = project.in(file("vam-model"))
    .settings(vamModelSettings)

lazy val vamServerSettings = baseSettings ++ Seq(
  libraryDependencies ++= {
    Seq(
      "com.typesafe" % "config" % configVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    )
  }
)

lazy val `vam-server` = project.in(file("vam-server"))
    .settings(vamServerSettings)
    .dependsOn(`vam-model`)

// OTHER SETTINGS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// -- PROMPT
// set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state =>
  val user = System.getProperty("user.name")
  user + "@" + Project.extract(state).currentRef.project + ":sbt> "
}


// -- VERSIONREPORT
// Add this setting to your project to generate a version report (See ExtendedBuild.scala too.)
// Use as 'sbt versionReport' or 'sbt version-report'
versionReport <<= (externalDependencyClasspath in Compile, streams) map {
  (cp: Seq[Attributed[File]], streams) =>
    val report = cp.map {
      attributed =>
        attributed.get(Keys.moduleID.key) match {
          case Some(moduleId) => "%40s %20s %10s %10s".format(
            moduleId.organization,
            moduleId.name,
            moduleId.revision,
            moduleId.configurations.getOrElse("")
          )
          case None =>
            // unmanaged JAR, just
            attributed.data.getAbsolutePath
        }
    }.sortBy(a => a.trim.split("\\s+").map(_.toUpperCase).take(2).last).mkString("\n")
    streams.log.info(report)
    report
}





import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

SbtScalariform.scalariformSettings

<<<<<<< HEAD
SbtScalariform.ScalariformKeys.preferences in ThisBuild := SbtScalariform.ScalariformKeys.preferences.value
   .setPreference(IndentSpaces, 2)
   .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
   .setPreference(DoubleIndentClassDeclaration, true)
   .setPreference(DanglingCloseParenthesis, Prevent)
=======
// -- SCALARIFORM
// Format code on save with scalariform
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

SbtScalariform.scalariformSettings

SbtScalariform.ScalariformKeys.preferences := SbtScalariform.ScalariformKeys.preferences.value
  .setPreference(IndentSpaces, 2)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
  .setPreference(DoubleIndentClassDeclaration, true)
>>>>>>> parent of 79579f3... Implementing messaging



// -- MISC
// fork a new JVM for run and test:run

// Aliases
addCommandAlias("cleanall", ";clean;clean-files")


