package org.eea.dataset.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class BasicSimulationExample //extends Simulation
{ // 3

  val httpConf = http.baseURL("http://localhost:8090")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val scn = scenario("BasicSimulation") // 7
    .exec(http("request_1") // 8
    .get("/actuator/health")) // 9
    .pause(5) // 10

  //  setUp( // 11
  //    scn.inject(atOnceUsers(1)) // 12
  //  ).protocols(httpConf) // 13
}
  
