package simulations

import java.util.UUID

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class LoadSimulation extends Simulation {

  val baseUrl = System.getProperty("TARGET_URL")

  val hostHeader = System.getProperty("HOST_HEADER", "")

  val httpConf = http.baseURL(baseUrl)

  private val rnd: Random = new Random()

  val headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json")
  val headersWithHost = if (!hostHeader.equals("")) headers + ("Host" -> hostHeader) else headers


  val randomFeedLowLatency = Iterator.continually(Map("randomId" -> UUID.randomUUID(), "randomDelay" -> (10 + rnd.nextInt(20))))
  val randomFeedHighLatency = Iterator.continually(Map("randomId" -> UUID.randomUUID(), "randomDelay" -> (1000 + rnd.nextInt(1000))))

  val scn1 = scenario("Messages - Low Latency")
    .feed(randomFeedLowLatency)
    .exec(repeat(1000) {
      exec(http("messages")
        .post("/messages")
        .headers(headersWithHost)
        .body(StringBody("""{"id": "${randomId}", "payload": "test payload", "delay": ${randomDelay}}""")))
        .pause(100 millis, 200 millis)
    })

  val scn2 = scenario("Messages - High Latency")
    .feed(randomFeedHighLatency)
    .exec(repeat(100) {
      exec(http("messages")
        .post("/messages")
        .headers(headersWithHost)
        .body(StringBody("""{"id": "${randomId}", "payload": "test payload", "delay": ${randomDelay}}""")))
    })

  setUp(scn1.inject(atOnceUsers(50)).protocols(httpConf), scn2.inject(atOnceUsers(6)).protocols(httpConf))
}
