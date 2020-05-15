/* CommonConfigPlugin.scala
 *
 * Copyright (c) 2015 True Connectivity Solutions Ltd.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.trueconnectivity

import sbt.Keys._
import sbt._

object CommonConfigPlugin extends AutoPlugin {

  import spray.revolver.RevolverPlugin.autoImport._
  import com.typesafe.sbt.GitPlugin
  import scoverage.ScoverageSbtPlugin
  import org.scalastyle.sbt.ScalastylePlugin
  import ScalastylePlugin.autoImport._
  import org.scalafmt.sbt.ScalafmtPlugin
  import ScalafmtPlugin.autoImport._

  override def requires: Plugins = empty

  override def trigger: PluginTrigger = allRequirements

  object CommonScalastyle {

    lazy val integrationTestScalastyle    = taskKey[Unit]("integrationTestScalastyle")
    lazy val testScalastyle    = taskKey[Unit]("testScalastyle")
    lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

    //Running scalastyle automatically on both compile and test
    lazy val settings = ScalastylePlugin.projectSettings ++ Seq(
      integrationTestScalastyle in ThisBuild := scalastyle.in(IntegrationTest).toTask("").value,
      testScalastyle in ThisBuild := scalastyle.in(Test).toTask("").value,
      compileScalastyle in ThisBuild := scalastyle.in(Compile).toTask("").value,
      (test in Test) := ((test in Test) dependsOn testScalastyle).value,
      (compile in Compile) := ((compile in Compile) dependsOn compileScalastyle).value
    )
  }

  object CommonScoverage {
    lazy val settings = ScoverageSbtPlugin.projectSettings
  }

  object CommonDependencies {
    val slf4j_version = "1.6.1"
    lazy val settings = Seq[Setting[_]](
      libraryDependencies in ThisBuild ++= Seq(
        "org.slf4j"    % "slf4j-api" % slf4j_version,
        "com.typesafe" % "config"    % "1.3.0"
      )
    )
  }

  object CommonCompile {
    lazy val settings = Seq[Setting[_]](
      scalacOptions ++= Seq(
        "-deprecation",
        "-feature",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Ywarn-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-adapted-args",
        "-Ywarn-inaccessible",
        "-unchecked"
      ),
      scalacOptions in Test ++= Seq(
        "-language:reflectiveCalls"
      )
    )
  }

  object CommonScalaFmt {

    import autoImport._

    private val sfmtConfFile  = "scalafmt.conf"
    private val styleConfFile = "scalastyle-config.xml"

    lazy val formattingTasksSettings = Seq[Setting[_]](
      generateConfigs := {
        IO.write(
          file(s".$sfmtConfFile"),
          IO.readBytes(getClass.getClassLoader().getResourceAsStream(sfmtConfFile))
        )
        IO.write(
          file(styleConfFile),
          IO.readBytes(getClass.getClassLoader().getResourceAsStream(styleConfFile))
        )
      },
      validate in ThisBuild := Def
        .sequential(
          (scalastyle in Compile).toTask(""),
          scalafmtCheckAll,
          scalafmtSbtCheck in Compile,
          Test / test
        )
        .value,
      format in ThisBuild := Def
        .sequential(
          scalafmtAll,
          scalafmtSbt in Compile
        )
        .value
    )
  }

  object autoImport {

    val generateConfigs: TaskKey[Unit] = taskKey[Unit](
      "Generates Lint & Formatting Configs"
    )

    val validate: TaskKey[Unit] =
      taskKey[Unit](
        "Validates the formatting and linting"
      )

    val format: TaskKey[Unit] =
      taskKey[Unit](
        "Format of the codes"
      )

    lazy val trueconnectivityCommonSettings: Seq[Def.Setting[_]] = Seq(
      organization in ThisBuild := "com.trueconnectivity",
      scalaVersion := "2.12.10"
    ) ++
      Revolver.settings ++
      CommonScalastyle.settings ++
      CommonDependencies.settings ++
      CommonCompile.settings ++
      CommonScoverage.settings ++
      net.virtualvoid.sbt.graph.DependencyGraphPlugin.projectSettings ++
      GitPlugin.projectSettings
  }

  // a group of settings that are automatically added to projects.
  import autoImport._

  override val projectSettings = Seq(Compile, Test).flatMap(
    inConfig(_)(trueconnectivityCommonSettings)
  ) ++ CommonScalaFmt.formattingTasksSettings

  override val buildSettings = GitPlugin.buildSettings
}
