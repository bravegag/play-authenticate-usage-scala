package actions

import com.feth.play.module.pa.PlayAuthenticate
import javax.inject.Inject
import play.api.mvc.{Action, BodyParser, Request, Result}
import play.mvc.Http

import scala.concurrent.Future

case class Auth[A](action: Action[A])(implicit auth: PlayAuthenticate) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
    if(!auth.isLoggedIn(Http.Context.current())) {
      auth.tryAuthenticateWithCookie(Http.Context.current())
    }

    action(request)
  }

  lazy val parser: BodyParser[A] = action.parser
}
