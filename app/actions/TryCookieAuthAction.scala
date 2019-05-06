package actions

import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc._
import play.core.j.JavaHelpers
import play.mvc.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import collection.JavaConverters._
import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._

/**
  * Action hook that attempts to authenticate the user using cookies.
  * @param action The inner action
  * @param auth the PlayAuthenticate framework
  * @tparam A The request body type
  */
case class TryCookieAuthAction[A](action: Http.Context => Action[A])(implicit auth: PlayAuthenticate) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
    val jContext = JavaHelpers.createJavaContext(request)

    TryCookieAuthAction.jContextDv += (request.id -> jContext)

    if(!auth.isLoggedIn(jContext)) {
      auth.tryAuthenticateWithCookie(jContext)
    }

    val scalaResult: Future[Result] = Await.ready(action(jContext)(request), 60 seconds)

    val session : Seq[(String, String)] = jContext.session().keySet().toArray.map(key => (key.toString, jContext.session().get(key)))
    val cookies : Seq[Cookie] = jContext.response().cookies().asScala.toSeq.map(cookie =>
      Cookie(cookie.name(), cookie.value(), maxAge = Option(cookie.maxAge()), path = cookie.path(), domain = Option(cookie.domain()),
        secure = cookie.secure(), httpOnly = cookie.httpOnly())
    )

    TryCookieAuthAction.jContextDv -= request.id

    scalaResult.map(_.withSession(session : _*).withCookies(cookies : _*))
  }
}

object TryCookieAuthAction {
  private lazy val jContextDv = TrieMap[Long, play.mvc.Http.Context]()

  /**
    * Extracts the Java context given a request
    * @param request The request
    * @tparam A The request body type
    */
  implicit class RequestToContext[A](request: Request[A]) {
    def jContextOption : Option[Http.Context] = jContextDv.get(request.id)
    def jContext : Http.Context = jContextDv(request.id)
  }

  def apply[A](action: Action[A])(implicit auth: PlayAuthenticate): TryCookieAuthAction[A] = TryCookieAuthAction(_ => action)
}
