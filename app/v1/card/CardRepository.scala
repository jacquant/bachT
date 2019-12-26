package v1.card

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.CustomExecutionContext
import slick.jdbc.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}
final case class CardData(
    id: CardId,
    title: String,
    content: String,
    status: String
)

class CardId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object CardId {
  def apply(raw: String): CardId = {
    require(raw != null)
    new CardId(Integer.parseInt(raw))
  }
}

class CardExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A repository for people.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class CardRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext
) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._

  /**
    * Here we define the table. It will have a name of people
    */
  private class MyCardTable(tag: Tag) extends Table[Card](tag, "card") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Int]("rowid", O.PrimaryKey, O.AutoInc)

    /** The title column */
    def title = column[String]("title")

    /** The ip column */
    def content = column[String]("content")

    def status = column[String]("status")

    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Card object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Card case classes
      * apply and unapply methods.
      */
    def * =
      (id, title, content, status) <> ((Card.apply _).tupled, Card.unapply)
  }

  /**
    * The starting point for all queries on the MyCard table.
    */
  private val card = TableQuery[MyCardTable]
  private val insertQuery = card returning card.map(_.id) into (
      (
          mycard,
          id
      ) => mycard.copy(id = id)
  )

  /**
    * Create a card with the given name
    *
    * This is an asynchronous operation, it will return a future of the created card, which can be used to obtain the
    * id for that card.
    */
  def create(
      title: String,
      content: String,
      status: String
  ): Future[Card] = {
    val action = insertQuery += Card(0, title, content, status)
    db.run(action)
  }

  /**
    * List all the card in the database.
    */
  def list(): Future[Seq[Card]] = db.run {
    card.result
  }

  def get(idCard: CardId): Future[Card] = db.run {
    card.filter(_.id === idCard.underlying).result.head
  }

  def delete(idCard: CardId): Future[Boolean] = {
    db.run(card.filter(_.id === idCard.underlying).delete).map { affectedRows =>
      affectedRows > 0
    }
  }

  def update(idCard: CardId, cardContent: Card): Future[Boolean] = {
    val newCard: Card = cardContent.copy(idCard.underlying)
    db.run(card.filter(_.id === idCard.underlying).update(newCard)).map {
      affectedRows =>
        affectedRows > 0
    }
  }
}
