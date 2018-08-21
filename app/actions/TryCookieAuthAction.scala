package actions

import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc._
import play.core.j.JavaHelpers
import play.mvc.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import collection.JavaConverters._

case class TryCookieAuthAction[A](action: Action[A])(implicit auth: PlayAuthenticate) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
    val jContext = JavaHelpers.createJavaContext(request)

    if(!auth.isLoggedIn(jContext)) {
      auth.tryAuthenticateWithCookie(jContext)
    }

    val session : Seq[(String, String)] = jContext.session().keySet().toArray.map(key => (key.toString, jContext.session().get(key)))
    val cookies : Seq[Cookie] = jContext.response().cookies().asScala.toSeq.map(cookie => Cookie(cookie.name(), cookie.value()))

    action(request).map(_.withSession(session : _*).withCookies(cookies : _*))
  }

  lazy val parser: BodyParser[A] = action.parser
}
