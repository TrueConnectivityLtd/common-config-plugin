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
import scalafix.sbt.ScalafixPlugin.autoImport.scalafixDependencies

object CommonConfigPlugin extends AutoPlugin {

  import com.typesafe.sbt.GitPlugin
  import org.scalastyle.sbt.ScalastylePlugin
  import ScalastylePlugin.autoImport._
  import org.scalafmt.sbt.ScalafmtPlugin
  import ScalafmtPlugin.autoImport._
  import scoverage.ScoverageSbtPlugin
  import spray.revolver.RevolverPlugin.autoImport._

  override def requires: Plugins = empty

  override def trigger: PluginTrigger = allRequirements

  object CommonScalastyle {

    lazy val integrationTestScalastyle = taskKey[Unit]("integrationTestScalastyle")
    lazy val testScalastyle            = taskKey[Unit]("testScalastyle")
    lazy val compileScalastyle         = taskKey[Unit]("compileScalastyle")

    private lazy val integrationTestSettings =
      if (project.configurations.contains(IntegrationTest)) {
        Seq(
          ThisBuild / integrationTestScalastyle := (IntegrationTest / scalastyle).toTask("").value,
          (IntegrationTest / compile) := ((IntegrationTest / compile) dependsOn integrationTestScalastyle).value
        )
      } else {
        Nil
      }

    lazy val settings: Seq[Def.Setting[_]] = ScalastylePlugin.projectSettings ++ Seq(
      ThisBuild / testScalastyle := (Test / scalastyle).toTask("").value,
      ThisBuild / compileScalastyle := (Compile / scalastyle).toTask("").value,
      (Test / test) := ((Test / test) dependsOn testScalastyle).value,
      (Compile / compile) := ((Compile / compile) dependsOn compileScalastyle).value
    ) ++ integrationTestSettings
  }

  object CommonScoverage {
    lazy val settings: Seq[sbt.Setting[_]] = ScoverageSbtPlugin.projectSettings
  }

  object CommonDependencies {
    val slf4j_version = "1.7.35"
    lazy val settings: Seq[sbt.Setting[_]] = Seq[Setting[_]](
      ThisBuild / libraryDependencies ++= Seq(
        "org.slf4j"    % "slf4j-api" % slf4j_version,
        "com.typesafe" % "config"    % "1.4.2"
      ),
      ThisBuild / scalafixDependencies ++= Seq(
        "com.github.liancheng" %% "organize-imports" % "0.6.0"
      )
    )
  }

  object CommonCompile {
    lazy val settings: Seq[sbt.Setting[_]] = Seq[Setting[_]](
      scalacOptions ++= Seq(
        "-deprecation",
        "-feature",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Ywarn-dead-code",
        "-Ywarn-unused",
        "-unchecked"
      ),
      Test / scalacOptions ++= Seq(
        "-language:reflectiveCalls"
      )
    )
  }

  object CommonScalaFmt {

    import autoImport._

    private val sfmtConfFile  = "scalafmt.conf"
    private val styleConfFile = "scalastyle-config.xml"

    lazy val formattingTasksSettings: Seq[sbt.Setting[_]] = Seq[Setting[_]](
      generateConfigs := {
        IO.write(
          file(s".$sfmtConfFile"),
          IO.readBytes(getClass.getClassLoader.getResourceAsStream(sfmtConfFile))
        )
        IO.write(
          file(styleConfFile),
          IO.readBytes(getClass.getClassLoader.getResourceAsStream(styleConfFile))
        )
      },
      ThisBuild / validate := Def
        .sequential(
          (Compile / scalastyle).toTask(""),
          scalafmtCheckAll,
          Compile / scalafmtSbtCheck,
          Test / test
        )
        .all(ScopeFilter(inAnyProject))
        .value,
      ThisBuild / format := Def
        .sequential(
          scalafmtAll,
          Compile / scalafmtSbt
        )
        .all(ScopeFilter(inAnyProject))
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
      ThisBuild / organization := "com.trueconnectivity",
      scalaVersion := "2.12.16"
    ) ++
      Revolver.settings ++
      CommonScalastyle.settings ++
      CommonDependencies.settings ++
      CommonCompile.settings ++
      CommonScoverage.settings ++
      GitPlugin.projectSettings
  }

  // a group of settings that are automatically added to projects.
  import autoImport._

  override val projectSettings: Seq[Def.Setting[_]] = Seq(Compile, Test).flatMap(
    inConfig(_)(trueconnectivityCommonSettings)
  ) ++ CommonScalaFmt.formattingTasksSettings

  override val buildSettings: Seq[sbt.Setting[_]] = GitPlugin.buildSettings
}
