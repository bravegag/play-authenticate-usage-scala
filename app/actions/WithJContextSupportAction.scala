package actions

import play.api._
import play.api.mvc._
import play.core.j.JavaHelpers
import play.core.j.JavaHelpers._
import scala.collection.concurrent.TrieMap
import scala.concurrent._
import scala.concurrent.duration._
import WithJContextSupportAction._

case class WithJContextSupportAction[A](block: JContext => Action[A])(implicit config: Configuration, env: Environment, bodyParsers: PlayBodyParsers, ec: ExecutionContext) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
    val components = JavaHelpers.createContextComponents(config, env)
    val jContext = createJavaContext(request, components)
    try {
      store += (request.id -> jContext)
      // need to wait for the enclosed actions to complete
      Await.ready(block(jContext)(request), 60 seconds)
    } finally {
      store -= request.id
    }
  }

  override def executionContext= ec
  override def parser= bodyParsers.default.asInstanceOf[BodyParser[A]]
}

object WithJContextSupportAction {
  type JContext = play.mvc.Http.Context
  type JComponents = play.core.j.JavaContextComponents
  private lazy val store = TrieMap[Long, JContext]()

  /**
    * Extracts the Java context given a request
    * @param request The request
    * @tparam A The request body type
    */
  implicit class RequestToContext[A](request: Request[A]) {
    def jContextOption : Option[JContext] = store.get(request.id)
    def jContext : JContext = store(request.id)
  }

  def apply[A](action: Action[A])(implicit config: Configuration, env: Environment, bodyParsers: PlayBodyParsers, ec: ExecutionContext): WithJContextSupportAction[A] = WithJContextSupportAction(_ => action)
}