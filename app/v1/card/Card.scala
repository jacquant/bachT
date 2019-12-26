package v1.card

import play.api.libs.json._

case class Card(
    id: Int,
    title: String,
    content: String,
    status: String
)

object Card {
  implicit val boardFormat: Format[Card] = Json.format
}
