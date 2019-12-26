package v1.card

import javax.inject._

import models._
import play.api.data.Form
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class CardFormInput(title: String, content: String)

class CardController @Inject()(
    repo: CardRepository,
    cc: CardControllerComponents
)(implicit ec: ExecutionContext)
    extends CardBaseController(cc) {

  private val form: Form[CardFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "content" -> nonEmptyText
      )(CardFormInput.apply)(CardFormInput.unapply)
    )
  }

  /**
    * A REST endpoint that gets all the cards as JSON.
    */
  def index: Action[AnyContent] = CardAction.async { implicit request =>
    cardResourceHandler.find.map { cards =>
      Ok(Json.toJson(cards))
    }
  }

  def process: Action[AnyContent] = CardAction.async { implicit request =>
    processJsonCardCreate()
  }

  def show(id: String): Action[AnyContent] = CardAction.async {
    implicit request =>
      cardResourceHandler.lookup(id).map { card =>
        Ok(Json.toJson(card))
      }
  }

  def delete(id: String): Action[AnyContent] = CardAction.async {
    implicit request =>
      cardResourceHandler.delete(id).map { card =>
        Ok(Json.toJson(card))
      }
  }

  def update(id: String): Action[AnyContent] = CardAction.async {
    implicit request =>
      processJsonCardUpdate(id = id)
  }

  private def processJsonCardCreate[A]()(
      implicit request: CardRequest[A]
  ): Future[Result] = {
    def failure(badForm: Form[CardFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: CardFormInput) = {
      cardResourceHandler.create(input).map { card =>
        Created(Json.toJson(card)).withHeaders()
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  private def processJsonCardUpdate[A](id: String)(
      implicit request: CardRequest[A]
  ): Future[Result] = {
    def failure(badForm: Form[CardFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: CardFormInput) = {
      cardResourceHandler.update(id, input).map { card =>
        Ok(Json.toJson(card))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
