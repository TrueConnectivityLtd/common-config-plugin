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

  import com.typesafe.sbt.SbtScalariform
  import com.typesafe.sbt.SbtScalariform.ScalariformKeys
  import spray.revolver.RevolverPlugin._
  import com.typesafe.sbt.GitPlugin
  import scoverage.ScoverageSbtPlugin
  import org.scalastyle.sbt.ScalastylePlugin


  object CommonScalastyle {
    lazy val testScalastyle = taskKey[Unit]("testScalastyle")
    lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

    //Running scalastyle automatically on both compile and test
    lazy val settings = ScalastylePlugin.projectSettings ++ Seq(
      testScalastyle := ScalastylePlugin.scalastyle.in(Test).toTask("").value,
      compileScalastyle := ScalastylePlugin.scalastyle.in(Compile).toTask("").value,
      (test in Test) <<= (test in Test) dependsOn testScalastyle,
      (compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle
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


  object autoImport {

    val generateConfig = SettingKey[Unit]("scalafmtGenerateConfig")

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
      net.virtualvoid.sbt.graph.Plugin.graphSettings ++
      GitPlugin.projectSettings
  }

  // a group of settings that are automatically added to projects.
  import autoImport._

  override val projectSettings =
    inConfig(Compile)(trueconnectivityCommonSettings) ++ inConfig(Test)(trueconnectivityCommonSettings)

  override val buildSettings = GitPlugin.buildSettings ++ SettingKey[Unit]("scalafmtGenerateConfig") :=
  IO.write(
      // writes to file once when build is loaded
      file(".scalafmt.conf"),
      """version = 2.2.1
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
}


/**
 * Symbolic link helper methods.
 */
object SymLink {
  def isSymLinkDirectory(dir: File): Boolean = dir.exists && dir.isDirectory && isSymLink(dir)

  def isSymLink(file: File): Boolean = file.exists && {
    val f = new File(file.getParentFile.getCanonicalFile, file.getName)
    f.getCanonicalPath != f.getAbsoluteFile.getPath
  }
}

/**
 * Class that uses all the symlinks present in the root folder of an sbt project and consider them as potential
 * sbt sub-projects.
 * @param id : the id/name of the root project.
 */
class CrossProjectBuild(id: String) extends Build {

  def findSymLinkedProjectFiles(cwd: File = file(".")): Seq[File] = {
    val currentSymLinkProjects = cwd.listFiles.filter(SymLink.isSymLinkDirectory)
    val allSymLinkProjects = for {
      dir <- currentSymLinkProjects
      symLinksUnderDir = findSymLinkedProjectFiles(dir)
    } yield (dir, symLinksUnderDir)

    val maybeUs = allSymLinkProjects.collectFirst {
      case (dir, symLinks) if dir.getName == id => symLinks.map(f => file(f.getName))
    }
    maybeUs getOrElse currentSymLinkProjects
  }

  lazy val symLinkedProjects = findSymLinkedProjectFiles().map(sp => RootProject(sp): ClasspathDep[ProjectReference])
  lazy val proj = Project(id = id, base = file("."), dependencies = symLinkedProjects)
    .settings(name := id)
    .settings(
      CommonConfigPlugin.projectSettings: _*
    )

}

