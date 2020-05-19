package com.trueconnectivity.e2e

import sbt._

trait E2eTestKeys {
  val dockerConnectPort         = settingKey[Int]("Applications exposed docker port")
  val dockerConnectRetryCount   = settingKey[Int]("Number of tries to successfully connect to the application")
  val dockerConnectRetryDelayMs = settingKey[Int]("Connection retry delay in millis")
}
