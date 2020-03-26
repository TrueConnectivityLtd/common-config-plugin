package com.trueconnectivity.buildinfo

import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import sbt.{Def, _}
import Keys._
import com.trueconnectivity.buildinfo.ReleaseVersionKeys._
import com.typesafe.sbt.SbtGit.GitKeys.{gitCurrentBranch, gitHeadCommit, gitUncommittedChanges}
import sbtbuildinfo.BuildInfoPlugin.autoImport.{BuildInfoKey, BuildInfoOption, buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo._

object ReleaseVersionPlugin extends AutoPlugin {
  private val GIT_SHORT_HASH_LENGTH = 7

  // no trigger - must be enabled explicitly
  override def requires: Plugins = BuildInfoPlugin

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    releaseVersion := {
      // Jenkins checks out specific commit, so branch returns commit SHA in that case - use EnvVar in Jenkins
      val branchName = Option(System.getenv("BRANCH_NAME")).getOrElse(gitCurrentBranch.value)
      Seq(
        Some(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))),
        gitHeadCommit.value.map(_.take(GIT_SHORT_HASH_LENGTH)),
        Some(branchName.filter(_.isLetterOrDigit)),
        if (gitUncommittedChanges.value) Some("SNAPSHOT") else None
      ).collect {
        case Some(segment) => segment
      }.mkString("-")
    },

    buildInfoPackage := "com.trueconnectivity.build",
    buildInfoKeys := Seq[BuildInfoKey](releaseVersion, scalaVersion),
    buildInfoOptions += BuildInfoOption.ToMap,

    releaseVersionFile := {
      val targetFile = baseDirectory.value / ".release_version"
      IO.write(targetFile, releaseVersion.value.getBytes(StandardCharsets.UTF_8))
      targetFile
    },

    (Compile / resourceGenerators) += releaseVersionFile.taskValue.map(Seq(_))
  )

}
