import sbt._
import Keys._

object ExtendedBuild extends Build {
  lazy val versionReport = TaskKey[String]("version-report", "Reports versions of dependencies")
  lazy val gitHeadCommitSha = SettingKey[String]("git-head", "Determines the current git commit SHA")
  lazy val makeVersionProperties = TaskKey[Seq[File]]("make-version-props", "Makes a version.properties file")
}
