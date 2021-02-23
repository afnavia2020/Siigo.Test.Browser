import scala.util.Random
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Config.url

class GetData extends Simulation {
  val headers = Map(
    "Content-Type" -> "application/json",
    "Accept"-> "*/*",
    "Accept-Encoding" -> "gzip, deflate, br"
  )

  val tokenFeeder = csv("src/test/resources/data/csv-files/tokens.csv").circular

  val rnd = new Random()

  val dataFeeder = Iterator.continually(Map(
    "browserId" -> (rnd.nextInt(62) + 1)
  ))

  def getData() = {
    feed(tokenFeeder)
    .feed(dataFeeder)
      .exec(http("Get Data")
          .post(url + "/GetData")
          .header("Authorization", "${tokens}")
          .body(ElFileBody("src/test/resources/data/json-files/get-data.json")).asJson
          .check(jsonPath("$").saveAs("bodyResponse"))
      )
      .exec{session => println(session("bodyResponse").as[String]);session}
      .pause(1)
  }

  val getDataScenario = scenario("Get Data")
    .exec(getData())

  setUp(getDataScenario.inject(
    atOnceUsers(10))
  )

}
