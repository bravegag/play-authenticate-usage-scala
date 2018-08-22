package actions

import com.feth.play.module.pa.PlayAuthenticate
import constants.FlashKey
import controllers.routes
import play.api.mvc._

import scala.concurrent.Future
import play.api.mvc.Results._

case class SudoForbidCookieAuthAction[A](action: Action[A])(implicit auth: PlayAuthenticate, ctx: play.mvc.Http.Context) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
    if (auth.isAuthorizedWithCookie(ctx)) {
      Future.successful(Redirect(routes.Application.relogin()).flashing(FlashKey.FLASH_ERROR_KEY -> "Please type password again to access requested page"))
    } else {
      action(request)
    }
  }

  lazy val parser: BodyParser[A] = action.parser
}
