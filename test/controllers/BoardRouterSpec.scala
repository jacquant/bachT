import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class BoardRouterSpec extends PlaySpec with GuiceOneAppPerTest {

  "BoarRouter" should {

    "render the list of boards" in {
      val request = FakeRequest(GET, "/v1/board")
        .withHeaders(HOST -> "localhost:9000")
        .withCSRFToken
      val home: Future[Result] = route(app, request).get

      val boards: Seq[BoardResource] =
        Json.fromJson[Seq[BoardResource]](contentAsJson(home)).get
      boards.filter(_.id == "1").head mustBe (BoardResource(
        "1",
        "/v1/board/1",
        "title 1",
        "blog board 1"
      ))
    }

    "render the list of boards when url ends with a trailing slash" in {
      val request = FakeRequest(GET, "/v1/board/")
        .withHeaders(HOST -> "localhost:9000")
        .withCSRFToken
      val home: Future[Result] = route(app, request).get

      val boards: Seq[BoardResource] =
        Json.fromJson[Seq[BoardResource]](contentAsJson(home)).get
      boards.filter(_.id == "1").head mustBe (BoarResource(
        "1",
        "/v1/board/1",
        "title 1",
        "blog board 1"
      ))
    }
  }

}
