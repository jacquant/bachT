package v1.card

import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying post information.
  */
case class CardResource(
    id: String,
    title: String,
    content: String,
    status: String
)

object CardResource {

  /**
    * Mapping to read/write a CardResource out as a JSON value.
    */
  implicit val format: Format[CardResource] = Json.format
}

/**
  * Controls access to the backend data, returning [[CardResource]]
  */
class CardResourceHandler @Inject()(
    routerProvider: Provider[CardRouter],
    cardRepository: CardRepository
)(implicit ec: ExecutionContext) {

  def create(
      cardInput: CardFormInput
  )(implicit mc: MarkerContext): Future[CardResource] = {
    val data = CardData(
      CardId("999"),
      cardInput.title,
      cardInput.content,
      cardInput.status
    )
    cardRepository
      .create(cardInput.title, cardInput.content, cardInput.status)
      .map { id =>
        createCardResourceFromCard(id)
      }
  }

  def lookup(
      id: String
  )(implicit mc: MarkerContext): Future[CardResource] = {
    val cardFuture = cardRepository.get(CardId(id))
    cardFuture.map { cardData =>
      createCardResourceFromCard(cardData)
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[CardResource]] = {
    cardRepository.list().map { cardDataList =>
      cardDataList.map(cardData => createCardResourceFromCard(cardData))
    }
  }

  def delete(id: String)(implicit mc: MarkerContext): Future[Boolean] = {
    cardRepository.delete(CardId(id))
  }

  def update(id: String, cardInput: CardFormInput)(
      implicit mc: MarkerContext
  ): Future[Boolean] = {
    val idCard = CardId(id)
    val data = Card(
      idCard.underlying,
      cardInput.title,
      cardInput.content,
      cardInput.status
    )
    cardRepository.update(idCard, data)
  }

  private def createCardResourceFromCardData(b: CardData): CardResource = {
    CardResource(
      b.id.toString(),
      b.title,
      b.content,
      b.status
    )
  }
  private def createCardResourceFromCard(b: Card): CardResource = {
    CardResource(
      b.id.toString(),
      b.title,
      b.content,
      b.status
    )
  }
}
