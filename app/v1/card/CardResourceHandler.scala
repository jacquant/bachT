package v1.card

import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import play.api.libs.json._
import scala.util.{Success, Failure}
import bacht._

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
    val creation = cardRepository.create(
      cardInput.title,
      cardInput.content,
      cardInput.status
    )
    tellToken(cardInput.title)
    creation.map { id =>
      createCardResourceFromCard(id)
    }
  }

  def lookup(
      id: String
  )(implicit mc: MarkerContext): Future[CardResource] = {
    val cardFuture = cardRepository.get(CardId(id))
    val cardFuture2: Future[Card] = cardRepository.get(CardId(id))
    cardFuture2
      .map(item => item.title)
      .onComplete {
        case Success(title) => askToken(title) // a voir si cela fonctionne
        case Failure(e)     => e.printStackTrace
      }
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
    val cardFuture: Future[Card] = cardRepository.get(CardId(id))
    cardFuture
      .map(item => item.title)
      .onComplete {
        case Success(title) => getToken(title)
        case Failure(e)     => e.printStackTrace
      }
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
    val cardFuture: Future[Card] = cardRepository.get(CardId(id))
    cardFuture
      .map(item => item.title)
      .onComplete {
        case Success(title) => updateToken(title, cardInput.title)
        case Failure(e)     => e.printStackTrace
      }

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

  private def tellToken(title: String): Unit = {
    val bachTRequest = "tell(" + title + ")"

    ag.apply(bachTRequest)
  }

  private def askToken(title: String): Unit = {
    val bachTRequest = "ask(" + title + ")"

    ag.apply(bachTRequest)
  }

  private def getToken(title: String): Unit = {
    val bachTRequest = "ask(" + title + ");get(" + title + ")"

    ag.apply(bachTRequest)
  }

  private def updateToken(oldTitle: String, newTitle: String): Unit = {
    getToken(oldTitle)
    tellToken(newTitle)
  }
}

object bs extends BachTStore {

  implicit val ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global

  def reset { clear_store }

  def intializeStore(card_repo: CardRepository): Unit = {
    val l_cards: Future[Seq[Card]] = card_repo.list()

    l_cards.foreach { c =>
      c.foreach { c_1 =>
        print(c_1.title)
      }
    }

  }
}

object ag extends BachTSimul(bs) {

  def apply(agent: String) {
    System.out.print("je suis en train d'appliquer")
    print(agent)
    val agent_parsed =
      BachTSimulParser.parse_agent(agent.replaceAll(" ", "_").toLowerCase())
    ag.bacht_exec_all(agent_parsed)
    bs.print_store
  }
  def eval(agent: String) { apply(agent) }
  def run(agent: String) { apply(agent) }

}
