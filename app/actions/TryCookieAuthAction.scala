package actions

import com.feth.play.module.pa.PlayAuthenticate
import helpers.ScalaRequestToJavaContext
import play.api.mvc._
import play.core.j.JavaHelpers
import play.mvc.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import collection.JavaConverters._
import scala.concurrent.duration._

case class TryCookieAuthAction[A](action: Http.Context => Action[A])(implicit auth: PlayAuthenticate) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
      val jContext = JavaHelpers.createJavaContext(request)

      ScalaRequestToJavaContext.put(request.id, jContext)

      if(!auth.isLoggedIn(jContext)) {
        auth.tryAuthenticateWithCookie(jContext)
      }

      val scalaResult: Future[Result] = Await.ready(action(jContext)(request), 60 seconds)

      val session : Seq[(String, String)] = jContext.session().keySet().toArray.map(key => (key.toString, jContext.session().get(key)))
      val cookies : Seq[Cookie] = jContext.response().cookies().asScala.toSeq.map(cookie =>
        Cookie(cookie.name(), cookie.value(), maxAge = Option(cookie.maxAge()), path = cookie.path(), domain = Option(cookie.domain()),
          secure = cookie.secure(), httpOnly = cookie.httpOnly())
      )

    ScalaRequestToJavaContext.delete(request.id)

      scalaResult.map(_.withSession(session : _*).withCookies(cookies : _*))
    }

  lazy val parser: BodyParser[A] = action(new Http.Context(new Http.RequestBuilder())).parser
}

object TryCookieAuthAction {
  def apply[A](action: Action[A])(implicit auth: PlayAuthenticate): TryCookieAuthAction[A] = TryCookieAuthAction(_ => action)
}
