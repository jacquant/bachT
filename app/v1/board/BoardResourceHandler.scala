package v1.board

import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying post information.
  */
case class BoardResource(
    id: String,
    name: String,
    ip: String,
    role: String,
    port: Int
)

object BoardResource {

  /**
    * Mapping to read/write a BoardResource out as a JSON value.
    */
  implicit val format: Format[BoardResource] = Json.format
}

/**
  * Controls access to the backend data, returning [[BoardResource]]
  */
class BoardResourceHandler @Inject()(
    routerProvider: Provider[BoardRouter],
    boardRepository: BoardRepository
)(implicit ec: ExecutionContext) {

  def create(
      boardInput: BoardFormInput
  )(implicit mc: MarkerContext): Future[BoardResource] = {
    val data = BoardData(
      BoardId("999"),
      boardInput.name,
      boardInput.ip,
      boardInput.role,
      boardInput.port
    )
    boardRepository
      .create(boardInput.name, boardInput.ip, boardInput.role, boardInput.port)
      .map { id =>
        createBoardResourceFromBoard(id)
      }
  }

  def lookup(
      id: String
  )(implicit mc: MarkerContext): Future[BoardResource] = {
    val boardFuture = boardRepository.get(BoardId(id))
    boardFuture.map { boardData =>
      createBoardResourceFromBoard(boardData)
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[BoardResource]] = {
    boardRepository.list().map { boardDataList =>
      boardDataList.map(boardData => createBoardResourceFromBoard(boardData))
    }
  }

  def delete(id: String)(implicit mc: MarkerContext): Future[Boolean] = {
    boardRepository.delete(BoardId(id))
  }

  def update(id: String, boardInput: BoardFormInput)(
      implicit mc: MarkerContext
  ): Future[Boolean] = {
    val idBoard = BoardId(id)
    val data = Board(
      idBoard.underlying,
      boardInput.name,
      boardInput.ip,
      boardInput.role,
      boardInput.port
    )
    boardRepository.update(idBoard, data)
  }

  private def createBoardResourceFromBoardData(b: BoardData): BoardResource = {
    BoardResource(
      b.id.toString(),
      b.name,
      b.ip,
      b.role,
      b.port
    )
  }
  private def createBoardResourceFromBoard(b: Board): BoardResource = {
    BoardResource(
      b.id.toString(),
      b.name,
      b.ip,
      b.role,
      b.port
    )
  }
}
