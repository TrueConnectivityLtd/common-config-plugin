package com.unshackled

import sbt.Keys._
import sbt._

object CommonConfigPlugin extends AutoPlugin {

  import com.typesafe.sbt.SbtScalariform
  import com.typesafe.sbt.SbtScalariform.ScalariformKeys
  import spray.revolver.RevolverPlugin._
  import com.typesafe.sbt.GitPlugin
  import scoverage.ScoverageSbtPlugin
  import org.scalastyle.sbt.ScalastylePlugin

  object CommonScalariform {
    lazy val settings = SbtScalariform.scalariformSettings ++ Seq(
      ScalariformKeys.preferences in Compile := formattingPreferences,
      ScalariformKeys.preferences in Test := formattingPreferences
    )

    import scalariform.formatter.preferences._

    def formattingPreferences =
      FormattingPreferences()
        .setPreference(RewriteArrowSymbols, true)
        .setPreference(AlignParameters, true)
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(DoubleIndentClassDeclaration, true)
  }

  object CommonScalastyle {
    lazy val testScalastyle = taskKey[Unit]("testScalastyle")
    lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

    //Running scalastyle automatically on both compile and test
    lazy val settings = ScalastylePlugin.projectSettings ++ Seq(
      testScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value,
      compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value,
      (test in Test) <<= (test in Test) dependsOn testScalastyle,
      (compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle
    )
  }

  object CommonDependencies {
    val slf4j_version = "1.6.1"
    lazy val settings = Seq[Setting[_]](

      libraryDependencies ++= Seq(
        "joda-time" % "joda-time" % "2.8.2",
        "org.slf4j" % "slf4j-simple" % slf4j_version,
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
    lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
      organization := "com.unshackled",
      scalaVersion := "2.11.7"
    ) ++ Seq(javaOptions ++=
      Seq("-Djava.awt.headless", "-Xmx1024m", "-XX:MaxMetaspaceSize=1024M")
    ) ++ Revolver.settings ++
      CommonScalariform.settings ++
      CommonScalastyle.settings ++
      CommonDependencies.settings ++
      CommonCompile.settings ++
      net.virtualvoid.sbt.graph.Plugin.graphSettings ++
      ScoverageSbtPlugin.projectSettings ++
      GitPlugin.projectSettings
  }

  // a group of settings that are automatically added to projects.
  import autoImport._

  override val projectSettings =
    inConfig(Compile)(commonSettings) ++ inConfig(Test)(commonSettings)

  override val buildSettings = GitPlugin.buildSettings

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

