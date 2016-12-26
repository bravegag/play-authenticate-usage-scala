package actions

import play.api.http.HeaderNames
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class NoCache[A](action: Action[A]) extends Action[A] with HeaderNames {
  def apply(request: Request[A]): Future[Result] = {
    action(request).map { result =>
      result.withHeaders(
        (CACHE_CONTROL -> "no-cache, no-store, must-revalidate"),
        (PRAGMA -> "no-cache"),
        (EXPIRES -> "0")
      )
    }
  }

  lazy val parser = action.parser
}
