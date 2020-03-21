package com.trueconnectivity.buildinfo

import java.io.File

import sbt._

object ReleaseVersionKeys {
  val releaseVersion = settingKey[String]("Application release version (to be used as docker tag)")
  val releaseVersionFile = taskKey[File]("Generate .release_version file in build root")
}
