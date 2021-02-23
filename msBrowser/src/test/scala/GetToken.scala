import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Config.url_security_service

class GetToken extends Simulation{

  val headers_1 = Map(
    "Content-Type" -> "application/x-www-form-urlencoded",
    "Authorization" -> "Basic U2lpZ29XZWI6QUJBMDhCNkEtQjU2Qy00MEE1LTkwQ0YtN0MxRTU0ODkxQjYx",
    "Accept" -> "application/json"
  )

  val csvFeeder = csv("src/test/resources/data/csv-files/users_mt34.csv").circular

  val tokens_writer = {
    val fos = new java.io.FileOutputStream("src/test/resources/data/csv-files/tokens.csv")
    new java.io.PrintWriter(fos,true)
  }

  //Crear el encabezado del archivo CSV
  tokens_writer.println("tokens")

  def getToken() = {
    feed(csvFeeder)
      .exec(http("GetToken")
        .post(url_security_service + "/connect/token?")
        .headers(headers_1)
        .body(StringBody("grant_type=password&username=${username}&password=1111&scope=WebApi offline_access"))
        .check(jsonPath("$.access_token").exists.saveAs("token")))
      .exec( session => {
        tokens_writer.println(session("token").as[String])
        session}
      )
      .pause(1)
  }

  val getTokenScenario = scenario("Get Token Scenario")
    .exec(getToken())

  setUp(getTokenScenario.inject(
    atOnceUsers(50))
  )

}
