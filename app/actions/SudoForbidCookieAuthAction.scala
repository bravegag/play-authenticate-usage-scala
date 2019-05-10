package actions

import com.feth.play.module.pa.PlayAuthenticate
import constants.{FlashKey, SessionKey}
import controllers.routes
import play.api.{Configuration, Environment}
import play.api.mvc._
import scala.concurrent.Future
import play.api.mvc.Results._
import WithJContextSupportAction._

/**
  * Custom action that checks whether an user is attempting to access a sudo protected area e.g. accessing payments
  * or security changes. This guard makes sure that the user is requested to re-enter the password again by redirecting
  * her to the re-login view.
  * @param action The inner action
  * @param auth The PlayAuthenticate singleton
  * @tparam A the type of the request body
  */
case class SudoForbidCookieAuthAction[A](action: Action[A])(implicit auth: PlayAuthenticate, config: Configuration, env: Environment, jContext: JContext) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
    if (auth.isAuthorizedWithCookie(jContext)) {
      // save url in session
      jContext.session().put(SessionKey.REDIRECT_TO_URI_KEY, request.uri)
      Future.successful(Redirect(routes.Application.relogin()).flashing(FlashKey.FLASH_ERROR_KEY -> "Please type password again to access requested page"))
    } else {
      action(request)
    }
  }

  override def executionContext = action.executionContext

  override val parser: BodyParser[A] = action.parser
}
