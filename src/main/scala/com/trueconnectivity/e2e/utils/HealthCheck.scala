package com.trueconnectivity.e2e.utils

import scalaj.http.{Http, HttpResponse}

import scala.util.{Failure, Success, Try}

object HealthCheck {
  def waitUntilHealthy(applicationPort: Int, retryCount: Int, retryDelay: Int): Unit = {
    retry(retryCount, retryDelay) {
      Http(s"http://localhost:${applicationPort}/health/status")
        .timeout(connTimeoutMs = 500, readTimeoutMs = 1000)
        .asString
    }
  }

  private def retry(retryCount: Int, retryDelay: Int)(fn: => HttpResponse[String]): Unit = {

    @annotation.tailrec
    def attemptConnection(
      count: Int = 0,
      msg: String = "Attempting connection "
    ): Try[HttpResponse[String]] = {
      println(msg)
      if (count >= retryCount) {
        throw new Exception("Maximum retry count was reached/exceeded waiting for service to start")
      }

      Thread.sleep(retryDelay)

      Try(fn) match {
        case Success(response) if !response.body.contains("UP") =>
          attemptConnection(count + 1, s"response ${response.code} => ${response.body}")
        case Failure(ex) =>
          attemptConnection(count + 1, s"response failed => error: ${ex.getMessage}")
        case result => result
      }
    }

    attemptConnection()
  }
}
