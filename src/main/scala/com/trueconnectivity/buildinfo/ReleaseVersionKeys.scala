package com.trueconnectivity.buildinfo

import java.io.File

import sbt._

object ReleaseVersionKeys {
  val appReleaseVersion =
    settingKey[String]("Application release version (to be used as docker tag)")
  val appReleaseVersionFile = taskKey[File]("Generate .release_version file in build root")
}
