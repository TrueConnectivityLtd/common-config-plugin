package com.trueconnectivity.e2e

import sbt._

trait E2eTestKeys {
  val instancePort = settingKey[Int]("Applications port in the container")
  val serviceName  = settingKey[String]("The name of the service in the docker-compose file")
  val e2eConnectRetryCount =
    settingKey[Int]("Number of tries to successfully connect to the application")
  val e2eConnectRetryDelayMs = settingKey[Int]("Connection retry delay in millis")
}
