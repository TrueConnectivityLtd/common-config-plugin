/* build.sbt
 *
 * Copyright (c) 2015 True Connectivity Solutions Ltd.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

name := "common-config-plugin"

sbtPlugin := true

scalaVersion := "2.12.16"

enablePlugins(SbtPlugin)

ivyLoggingLevel := UpdateLogging.Quiet
Compile / scalacOptions ++= Seq("-feature", "-deprecation")

releaseVersionBump := sbtrelease.Version.Bump.Bugfix

organization := "com.trueconnectivity"

resolvers ++= Seq(
  Resolver.defaultLocal,
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("releases"),
  Resolver.typesafeRepo("releases"),
  Resolver.typesafeRepo("snapshots"),
  Resolver.sonatypeRepo("snapshots")
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.2.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.2")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9")

addSbtPlugin("com.trueconnectivity" % "sbt-docker-compose" % "1.0.38")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.0")

publishMavenStyle := true
Test / publishArtifact := false
pomIncludeRepository := { _ =>
  false
}

addCommandAlias("format", "; scalafmt ; test:scalafmt ; scalafmtSbt")
//Potential other plugins to add : unidoc
