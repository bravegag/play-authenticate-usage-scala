package actions

import com.feth.play.module.pa.PlayAuthenticate
import play.api._
import play.api.mvc._
import support.JContextSupport

import collection.JavaConverters._
import scala.concurrent._

case class TryCookieAuthAction[A](action: Action[A])(implicit auth: PlayAuthenticate, config: Configuration, env: Environment, ec: ExecutionContext) extends Action[A] with JContextSupport {
  def apply(request: Request[A]): Future[Result] = {
    withContext(request) { jContext =>
      if (!auth.isLoggedIn(jContext)) {
        auth.tryAuthenticateWithCookie(jContext)
      }

      action(request).map { result: Result =>
        val session: Seq[(String, String)] = jContext.session().keySet().toArray.map(key => (key.toString, jContext.session().get(key)))
        val cookies: Seq[Cookie] = jContext.response().cookies().asScala.toSeq.map(cookie =>
          Cookie(cookie.name(), cookie.value(), maxAge = Option(cookie.maxAge()), path = cookie.path(), domain = Option(cookie.domain()),
            secure = cookie.secure(), httpOnly = cookie.httpOnly())
        )
        result.withSession(session: _*).withCookies(cookies: _*)
      }
    }
  }

  override def parser           = action.parser
  override def executionContext = action.executionContext
}
