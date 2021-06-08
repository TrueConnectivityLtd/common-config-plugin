/* build.sbt
 *
 * Copyright (c) 2015 True Connectivity Solutions Ltd.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

name := "common-config-plugin"

sbtPlugin := true

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

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.8.2")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.6")

addSbtPlugin("com.tapad" % "sbt-docker-compose" % "1.0.35")

publishMavenStyle := true
Test / publishArtifact := false
pomIncludeRepository := { _ =>
  false
}

addCommandAlias("format", "; scalafmt ; test:scalafmt ; scalafmtSbt")

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"

//Potential other plugins to add : unidoc
