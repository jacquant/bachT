package v1.board

import javax.inject._

import models._
import play.api.data.Form
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class BoardFormInput(name: String, ip: String, role: String, port: Int)

class BoardController @Inject()(
    repo: BoardRepository,
    cc: BoardControllerComponents
)(implicit ec: ExecutionContext)
    extends BoardBaseController(cc) {

  private val form: Form[BoardFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "name" -> nonEmptyText,
        "ip" -> nonEmptyText,
        "role" -> nonEmptyText,
        "port" -> number
      )(BoardFormInput.apply)(BoardFormInput.unapply)
    )
  }

  /**
    * A REST endpoint that gets all the boards as JSON.
    */
  def index: Action[AnyContent] = BoardAction.async { implicit request =>
    boardResourceHandler.find.map { boards =>
      Ok(Json.toJson(boards))
    }
  }

  def process: Action[AnyContent] = BoardAction.async { implicit request =>
    processJsonBoardCreate()
  }

  def show(id: String): Action[AnyContent] = BoardAction.async {
    implicit request =>
      boardResourceHandler.lookup(id).map { board =>
        Ok(Json.toJson(board))
      }
  }

  def delete(id: String): Action[AnyContent] = BoardAction.async {
    implicit request =>
      boardResourceHandler.delete(id).map { board =>
        Ok(Json.toJson(board))
      }
  }

  def update(id: String): Action[AnyContent] = BoardAction.async {
    implicit request =>
      processJsonBoardUpdate(id = id)
  }

  private def processJsonBoardCreate[A]()(
      implicit request: BoardRequest[A]
  ): Future[Result] = {
    def failure(badForm: Form[BoardFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: BoardFormInput) = {
      boardResourceHandler.create(input).map { board =>
        Created(Json.toJson(board)).withHeaders()
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  private def processJsonBoardUpdate[A](id: String)(
      implicit request: BoardRequest[A]
  ): Future[Result] = {
    def failure(badForm: Form[BoardFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: BoardFormInput) = {
      boardResourceHandler.update(id, input).map { board =>
        Ok(Json.toJson(board))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
