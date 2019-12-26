package v1.board

import play.api.libs.json._

case class Board(
    id: Int,
    name: String,
    ip: String,
    role: String,
    port: Int
)

object Board {
  implicit val boardFormat: Format[Board] = Json.format
}
