package support

import play.api._
import play.api.i18n.Lang
import play.api.mvc.{Request, RequestHeader}
import play.core.j.JavaHelpers
import play.mvc.Http
import scala.collection.concurrent.TrieMap

trait JContextSupport {
  import JavaHelpers._
  import JContextSupport._

  implicit def toComponents(implicit config: Configuration, env: Environment): JComponents =
    JavaHelpers.createContextComponents(config, env)
/*
  implicit def request2Context(implicit request: RequestHeader, config: Configuration, env: Environment): JContext =
    createJavaContext(request, JavaHelpers.createContextComponents(config, env))

  implicit def request2Context(implicit request: RequestHeader, components: JComponents): JContext =
    createJavaContext(request, components)
*/
  def withContext[A](block: JContext => A)(implicit request: RequestHeader, components: JComponents): A = {
    implicit val jContext = createJavaContext(request, components)
    try {
      JContextSupport.store += (request.id -> jContext)
      block(jContext)
    } finally {
      JContextSupport.store -= request.id
    }
  }

  def withContext[A, B](actual: Request[A])(block: JContext => B)(implicit components: JComponents): B = {
    implicit val request: RequestHeader = actual.asInstanceOf[RequestHeader]
    withContext(block)
  }

  implicit def jContext(implicit request: RequestHeader): JContext = {
    store.get(request.id).getOrElse(
      throw new IllegalStateException(s"jContext not found for request with id ${request.id}.")
    )
  }

  implicit def lang(implicit request: RequestHeader): Lang = request.acceptLanguages.head
}

object JContextSupport {
  type JContext = play.mvc.Http.Context
  type JComponents = play.core.j.JavaContextComponents
  private lazy val store = TrieMap[Long, JContext]()

  /**
    * Extracts the Java context given a request
    * @param request The request
    * @tparam A The request body type
    */
  implicit class RequestToContext[A](request: Request[A]) {
    def jContextOption : Option[Http.Context] = store.get(request.id)
    def jContext : Http.Context = store(request.id)
  }
}