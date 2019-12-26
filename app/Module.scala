import com.google.inject.AbstractModule
import javax.inject._
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import v1.board._
import v1.card._
import bacht._

/**
  * Sets up custom components for Play.
  *
  * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
  */
class Module(environment: Environment, configuration: Configuration)
    extends AbstractModule
    with ScalaModule {}
