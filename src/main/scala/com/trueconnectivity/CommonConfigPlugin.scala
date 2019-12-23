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
import scoverage.ScoverageKeys

object CommonConfigPlugin extends AutoPlugin {

  import spray.revolver.RevolverPlugin.autoImport._
  import com.typesafe.sbt.GitPlugin
  import scoverage.ScoverageSbtPlugin
  import org.scalastyle.sbt.ScalastylePlugin
  import ScalastylePlugin.autoImport._
  import org.scalafmt.sbt.ScalafmtPlugin
  import ScalafmtPlugin.autoImport._

  object CommonScalastyle {

    lazy val testScalastyle = taskKey[Unit]("testScalastyle")
    lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

    //Running scalastyle automatically on both compile and test
    lazy val settings = ScalastylePlugin.projectSettings ++ Seq(
      testScalastyle := scalastyle.in(Test).toTask("").value,
      compileScalastyle := scalastyle.in(Compile).toTask("").value,
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

      libraryDependencies ++= Seq(
        "org.slf4j" % "slf4j-api" % slf4j_version,
        "com.typesafe" % "config" % "1.3.0"
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

    val generateConfig = SettingKey[Unit]("scalafmtGenerateConfig")

    lazy val buildSettings = SettingKey[Unit]("scalafmtGenerateConfig") :=
    IO.write(
        // writes to file once when build is loaded
        file(".scalafmt.conf"),
        """
        |version = 2.2.1
        |style = defaultWithAlign
        |maxColumn = 100
        |project {
        |  git = true
        |}
        |align {
        |  openParenCallSite = false
        |  openParenDefnSite = false
        |}
        |binPack {
        |  parentConstructors = true
        |}
        |
        |continuationIndent {
        |  callSite = 2
        |  defnSite = 4
        |}
        |
        |danglingParentheses = true
        |
        |rewrite.rules = [RedundantBraces, RedundantParens, PreferCurlyFors]
        |
        |align.openParenCallSite = false
        """.stripMargin.getBytes("UTF-8")
      )

    lazy val formattingTasksSettings = Seq[Setting[_]](validate := Def
    .sequential(
      (scalastyle in Compile).toTask(""),
      scalafmtCheckAll,
      scalafmtSbtCheck in Compile
      //           scapegoat
    )
    .value,
  format := Def
    .sequential(
      scalafmtAll,
      scalafmtSbt in Compile
    )
    .value)
  }

  object autoImport {

    val validate: TaskKey[Unit] =
      taskKey[Unit](
        "Validates the formatting and linting"
      )

    val format: TaskKey[Unit] =
      taskKey[Unit](
        "Format of the codes"
      )

    lazy val trueconnectivityCommonSettings: Seq[Def.Setting[_]] = Seq(
      organization := "com.trueconnectivity",
      scalaVersion := "2.12.10"
    ) ++ Seq(javaOptions ++=
      Seq("-Djava.awt.headless", "-Xmx1024m", "-XX:MaxMetaspaceSize=1024M")
    ) ++ Revolver.settings ++
      CommonScalastyle.settings ++
      CommonDependencies.settings ++
      CommonCompile.settings ++
      CommonScoverage.settings ++
      net.virtualvoid.sbt.graph.DependencyGraphPlugin.projectSettings ++
      GitPlugin.projectSettings
  }

  // a group of settings that are automatically added to projects.
  import autoImport._

  override val projectSettings = CommonScalaFmt.formattingTasksSettings ++
    inConfig(Compile)(trueconnectivityCommonSettings) ++ inConfig(Test)(trueconnectivityCommonSettings)

  override val buildSettings = GitPlugin.buildSettings ++ CommonScalaFmt.buildSettings
}
