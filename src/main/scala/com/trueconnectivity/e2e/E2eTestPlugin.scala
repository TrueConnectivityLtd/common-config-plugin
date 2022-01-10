package com.trueconnectivity.e2e

import com.tapad.docker.DockerComposePlugin
import com.trueconnectivity.e2e.utils.HealthCheck
import com.typesafe.sbt.packager.docker.DockerPlugin
import sbt.Keys._
import sbt._

import java.io.File

object E2eTestPlugin extends AutoPlugin {
  override val requires: Plugins      = DockerPlugin
  override val trigger: PluginTrigger = allRequirements

  object autoImport extends E2eTestKeys
  import autoImport._

  override lazy val projectConfigurations: Seq[Configuration] = Seq(
    IntegrationTest
  )

  object E2eConfig {
    import DockerComposePlugin.autoImport._

    lazy val settings: Seq[Setting[_]] = Seq(
      testCasesJar := (IntegrationTest / packageBin / artifactPath).value.getAbsolutePath,
      testCasesPackageTask := {
        HealthCheck.waitUntilHealthy(
          serviceName.value,
          instancePort.value,
          e2eConnectRetryCount.value,
          e2eConnectRetryDelayMs.value
        )

        (IntegrationTest / sbt.Keys.packageBin).value
      },
      testDependenciesClasspath := (
        (Compile / fullClasspath).value.files ++
          (IntegrationTest / managedClasspath).value.files ++
          (IntegrationTest / unmanagedClasspath).value.files ++
          (IntegrationTest / resources).value ++
          Seq((IntegrationTest / classDirectory).value)
      ).map(_.getAbsoluteFile)
        .mkString(File.pathSeparator),
      dockerImageCreationTask := (DockerPlugin.autoImport.Docker / publishLocal).value
    )
  }

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    e2eConnectRetryCount := 10,
    e2eConnectRetryDelayMs := 4000
  ) ++ Defaults.itSettings ++ DockerComposePlugin.projectSettings ++ E2eConfig.settings

  override lazy val buildSettings: Seq[Setting[_]] = Seq()
}
