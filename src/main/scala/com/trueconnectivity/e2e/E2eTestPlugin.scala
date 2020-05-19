package com.trueconnectivity.e2e

import com.tapad.docker.DockerComposePlugin
import com.typesafe.sbt.packager.docker.DockerPlugin
import sbt._
import Keys._
import java.io.File

import com.trueconnectivity.e2e.utils.HealthCheck

object E2eTestPlugin extends AutoPlugin {
  override val requires: Plugins = DockerPlugin
  override val trigger: PluginTrigger = allRequirements

  object autoImport extends E2eTestKeys
  import autoImport._

  override lazy val projectConfigurations: Seq[Configuration] = Seq(
    IntegrationTest
  )

  object E2eConfig {
    import DockerComposePlugin.autoImport._

    lazy val settings: Seq[Setting[_]] = Seq(
      testCasesJar := artifactPath
        .in(IntegrationTest, packageBin)
        .value
        .getAbsolutePath,
      testCasesPackageTask := {
        HealthCheck.waitUntilHealthy(
          e2eConnectPort.value,
          e2eConnectRetryCount.value,
          e2eConnectRetryDelayMs.value
        )

        (sbt.Keys.packageBin in IntegrationTest).value
      },
      testDependenciesClasspath := (
          (managedClasspath in IntegrationTest).value.files ++
          (unmanagedClasspath in IntegrationTest).value.files ++
          (resources in IntegrationTest).value ++
          Seq((classDirectory in IntegrationTest).value)
        )
        .map(_.getAbsoluteFile)
        .mkString(File.pathSeparator),
      dockerImageCreationTask := (publishLocal in DockerPlugin.autoImport.Docker).value
    )
  }

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    e2eConnectRetryCount := 10,
    e2eConnectRetryDelayMs := 4000,
  ) ++ Defaults.itSettings ++ DockerComposePlugin.projectSettings ++ E2eConfig.settings

  override lazy val buildSettings: Seq[Setting[_]] = Seq()
}
