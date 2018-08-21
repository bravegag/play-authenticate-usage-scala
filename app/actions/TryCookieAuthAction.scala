package actions

import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc.{Action, BodyParser, Request, Result}
import play.core.j.JavaHelpers

import scala.concurrent.Future

case class TryCookieAuthAction[A](action: Action[A])(implicit auth: PlayAuthenticate) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
    val jContext = JavaHelpers.createJavaContext(request)
    if(!auth.isLoggedIn(jContext)) {
      auth.tryAuthenticateWithCookie(jContext)
    }

    action(request)
  }

  lazy val parser: BodyParser[A] = action.parser
}
