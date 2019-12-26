package v1.card

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes and URLs to the PostResource controller.
  */
class CardRouter @Inject()(controller: CardController) extends SimpleRouter {
  val prefix = "/v1/card"

  def link(id: CardId): String = {
    import io.lemonlabs.uri.dsl._
    val url = prefix / id.toString
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.process

    case GET(p"/$id") =>
      controller.show(id)

    case DELETE(p"/$id") =>
      controller.delete(id)

    case PUT(p"/$id") =>
      controller.update(id)
  }

}
