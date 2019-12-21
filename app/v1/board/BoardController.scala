package v1.board

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class BoardFormInput(title: String, body: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class BoardController @Inject()(cc: BoardControllerComponents)(
  implicit ec: ExecutionContext)
  extends BoardBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[BoardFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "body" -> text
      )(BoardFormInput.apply)(BoardFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = BoardAction.async { implicit request =>
    logger.trace("index: ")
    boardResourceHandler.find.map { boards =>
      Ok(Json.toJson(boards))
    }
  }

  def process: Action[AnyContent] = BoardAction.async { implicit request =>
    logger.trace("process: ")
    processJsonBoard()
  }

  def show(id: String): Action[AnyContent] = BoardAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      boardResourceHandler.lookup(id).map { board =>
        Ok(Json.toJson(board))
      }
  }

  private def processJsonBoard[A]()(
    implicit request: BoardRequest[A]): Future[Result] = {
    def failure(badForm: Form[BoardFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: BoardFormInput) = {
      boardResourceHandler.create(input).map { board =>
        Created(Json.toJson(board)).withHeaders(LOCATION -> board.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
