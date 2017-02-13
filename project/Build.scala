import sbt.Keys._
import sbt._

object Build extends Build {

  override lazy val settings = super.settings ++
    Seq(scalaVersion := "2.11.8", exportJars := true) ++
    Seq(scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := false,
      scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := true,
      scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 80,
      scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "com.yaochin.battleship.injection.*")

  lazy val root = Project(
    id = "battleship-service",
    base = file("."),
    settings = settings ++ Seq(publishArtifact := false)
  ).aggregate(server, serverFeatureTest)

  lazy val server = Project(
    id = "server",
    base = file("battleship-server"),
    settings = settings
  )

  lazy val serverFeatureTest = Project(
    id = "server-feature-test",
    base = file("battleship-server-feature-test"),
    settings = settings
  ) .dependsOn(server % "test->test;test->compile")
}