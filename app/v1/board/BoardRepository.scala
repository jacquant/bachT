package v1.board

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

final case class BoardData(id: BoardId, title: String, body: String)

class BoardId private(val underlying: Int) extends AnyVal {
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
  * A pure non-blocking interface for the BoardRepository.
  */
trait BoardRepository {
  def create(data: BoardData)(implicit mc: MarkerContext): Future[BoardId]

  def list()(implicit mc: MarkerContext): Future[Iterable[BoardData]]

  def get(id: BoardId)(implicit mc: MarkerContext): Future[Option[BoardData]]
}

/**
  * A trivial implementation for the Board Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class BoardRepositoryImpl @Inject()()(implicit ec: BoardExecutionContext)
  extends BoardRepository {

  private val logger = Logger(this.getClass)

  private val boardList = List(
    BoardData(BoardId("1"), "title 1", "board 1"),
    BoardData(BoardId("2"), "title 2", "board 2"),
    BoardData(BoardId("3"), "title 3", "board 3"),
    BoardData(BoardId("4"), "title 4", "board 4"),
    BoardData(BoardId("5"), "title 5", "board 5")
  )

  override def list()(
    implicit mc: MarkerContext): Future[Iterable[BoardData]] = {
    Future {
      logger.trace(s"list: ")
      boardList
    }
  }

  override def get(id: BoardId)(
    implicit mc: MarkerContext): Future[Option[BoardData]] = {
    Future {
      logger.trace(s"get: id = $id")
      boardList.find(board => board.id == id)
    }
  }

  def create(data: BoardData)(implicit mc: MarkerContext): Future[BoardId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
