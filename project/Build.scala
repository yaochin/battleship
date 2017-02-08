import sbt.Keys._
import sbt._

object Build extends Build {

  override lazy val settings = super.settings ++
    Seq(scalaVersion := "2.11.8", exportJars := true)

  lazy val root = Project(
    id = "battleship-service",
    base = file("."),
    settings = settings ++ Seq(publishArtifact := false)
  ).aggregate(server)

  lazy val server = Project(
    id = "server",
    base = file("battleship-server"),
    settings = settings
  )
}