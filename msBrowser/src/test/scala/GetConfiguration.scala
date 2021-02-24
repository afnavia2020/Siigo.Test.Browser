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
  val httpProtocol = http
		.baseUrl("https://http://qastaging.siigo.com/ISIIGO/Login.aspx")
    .inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-GB,en-US;q=0.9,en;q=0.8")
		.upgradeInsecureRequestsHeader("1")
		.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36")

  val tokensFeeder = csv("tokens.csv").circular

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
      rampUsersPerSec(5) to (10) during(3 minutes)
    )
  )

}
