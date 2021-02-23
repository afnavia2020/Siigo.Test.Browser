import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.util.Random
import Config.url
import scala.concurrent.duration._

class GetConfiguration extends Simulation{
  var headers = Map(
    "Content-Type" -> "application/json",
    "Accept" -> "*/*",
    "Accept-Encoding" -> "gzip, deflate, br"
  )

  val tokensFeeder = csv("src/test/resources/data/csv-files/tokens.csv").circular

  val rnd = new Random()

  val browserFeeder = Iterator.continually(Map(
    "browserId" -> (rnd.nextInt(14) + 1)
  ))

  def getConfiguration() = {
    feed(tokensFeeder)
    .feed(browserFeeder)
      .exec(http("Get Configuration")
          .post(url + "/GetConfiguration")
          .headers(headers)
          .header("Authorization", "${tokens}")
          .body(StringBody("""{"type":0,"browserID":"${browserId}"}""")).asJson
          .check(jsonPath("$").saveAs("bodyResponse"))
          .check(status.is(200))
        .check(status.saveAs("status"))
      )
      .doIf(session => session("status").as[String].equals("500"))
      {exec(session => {
        println(session("browserId").as[String]+", "+session("tokens").as[String])
        session})
      }
      .pause(2)
  }

  val getConfigurationScenario = scenario("Get Configuration scenario")
    .exec(getConfiguration())

  setUp(
    getConfigurationScenario.inject(
      atOnceUsers(5),
      rampUsersPerSec(5) to (100) during(15 minutes)
    )
  )

}
