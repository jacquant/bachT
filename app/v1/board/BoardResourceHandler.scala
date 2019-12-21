package v1.board

import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying post information.
  */
case class BoardResource(id: String, link: String, title: String, body: String)

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
                                      boardRepository: BoardRepository)(implicit ec: ExecutionContext) {

  def create(boardInput: BoardFormInput)(
    implicit mc: MarkerContext): Future[BoardResource] = {
    val data = BoardData(BoardId("999"), boardInput.title, boardInput.body)
    // We don't actually create the board, so return what we have
    boardRepository.create(data).map { id =>
      createBoardResource(data)
    }
  }

  def lookup(id: String)(
    implicit mc: MarkerContext): Future[Option[BoardResource]] = {
    val boardFuture = boardRepository.get(BoardId(id))
    boardFuture.map { maybeBoardData =>
      maybeBoardData.map { boardData =>
        createBoardResource(boardData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[BoardResource]] = {
    boardRepository.list().map { boardDataList =>
      boardDataList.map(boardData => createBoardResource(boardData))
    }
  }

  private def createBoardResource(b: BoardData): BoardResource = {
    BoardResource(b.id.toString, routerProvider.get.link(b.id), b.title, b.body)
  }

}
