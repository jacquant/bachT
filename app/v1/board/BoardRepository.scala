package v1.board

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.CustomExecutionContext
import slick.jdbc.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}
final case class BoardData(
    id: BoardId,
    name: String,
    ip: String,
    role: String,
    port: Int
)

class BoardId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object BoardId {
  def apply(raw: String): BoardId = {
    require(raw != null)
    new BoardId(Integer.parseInt(raw))
  }
}

class BoardExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A repository for people.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class BoardRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(
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
  private class MyBoardTable(tag: Tag) extends Table[Board](tag, "board") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Int]("rowid", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")

    /** The ip column */
    def ip = column[String]("ip")

    /** The role column */
    def role = column[String]("role")

    /** The port column */
    def port = column[Int]("port")

    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Board object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Board case classes
      * apply and unapply methods.
      */
    def * =
      (id, name, ip, role, port) <> ((Board.apply _).tupled, Board.unapply)
  }

  /**
    * The starting point for all queries on the MyBoard table.
    */
  private val board = TableQuery[MyBoardTable]
  private val insertQuery = board returning board.map(_.id) into (
      (
          myboard,
          id
      ) => myboard.copy(id = id)
  )

  /**
    * Create a board with the given name
    *
    * This is an asynchronous operation, it will return a future of the created board, which can be used to obtain the
    * id for that board.
    */
  def create(
      name: String,
      ip: String,
      role: String,
      port: Int
  ): Future[Board] = {
    val action = insertQuery += Board(0, name, ip, role, port)
    db.run(action)
  }

  /**
    * List all the board in the database.
    */
  def list(): Future[Seq[Board]] = db.run {
    board.result
  }

  def get(idBoard: BoardId): Future[Board] = db.run {
    board.filter(_.id === idBoard.underlying).result.head
  }

  def delete(idBoard: BoardId): Future[Boolean] = {
    db.run(board.filter(_.id === idBoard.underlying).delete).map {
      affectedRows =>
        affectedRows > 0
    }
  }

  def update(idBoard: BoardId, boardContent: Board): Future[Boolean] = {
    val newBoard: Board = boardContent.copy(idBoard.underlying)
    db.run(board.filter(_.id === idBoard.underlying).update(newBoard)).map {
      affectedRows =>
        affectedRows > 0
    }
  }
}
