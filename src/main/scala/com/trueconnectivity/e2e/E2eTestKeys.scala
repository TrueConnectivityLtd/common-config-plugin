package com.trueconnectivity.e2e

import sbt._

trait E2eTestKeys {
  val e2eConnectPort         = settingKey[Int]("Applications exposed docker port")
  val e2eConnectRetryCount   = settingKey[Int]("Number of tries to successfully connect to the application")
  val e2eConnectRetryDelayMs = settingKey[Int]("Connection retry delay in millis")
}
