package actions

import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc._
import play.core.j.JavaHelpers
import play.mvc.Http

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class JavaContext(action: Http.Context => play.mvc.Result)(implicit auth: PlayAuthenticate) extends Action[AnyContent] {
  def apply(request: Request[AnyContent]): Future[Result] =
    Future {
      val jContext = JavaHelpers.createJavaContext(request)

      if(!auth.isLoggedIn(jContext)) {
        auth.tryAuthenticateWithCookie(jContext)
      }

      val session : Seq[(String, String)] = jContext.session().keySet().toArray.map(key => (key.toString, jContext.session().get(key)))
      val cookies : Seq[Cookie] = jContext.response().cookies().asScala.toSeq.map(cookie => Cookie(cookie.name(), cookie.value()))

      val javaResult = action(request)

      JavaHelpers.createResult(jContext, javaResult)
    }

  lazy val parser: BodyParser[AnyContent] = Action { Results.Ok() }.parser
}
