package simulations

import java.util.UUID

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class KNativeSimulation extends Simulation {

  val baseUrl = System.getProperty("TARGET_URL")
  val sim_users = System.getProperty("SIM_USERS").toInt
  val hostHeader = System.getProperty("HOST_HEADER", "")

  val httpConf = http.baseURL(baseUrl)

  val headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json")
  val headersWithHost = if (!hostHeader.equals("")) headers + ("Host" -> hostHeader) else headers

  val messagesApiCall = repeat(30) {
    exec(http("messages")
      .post("/messages")
      .headers(headersWithHost)
      .body(StringBody(
        s"""
           | {
           |   "id": "${UUID.randomUUID().toString}",
           |   "payload": "test payload",
           |   "delay": 300
           | }
        """.stripMargin)))
      .pause(1 second, 2 seconds)
  }

  val scn = scenario("Messages API")
    .exec(messagesApiCall)

  setUp(scn.inject(rampUsers(sim_users).over(30 seconds)).protocols(httpConf))
}
