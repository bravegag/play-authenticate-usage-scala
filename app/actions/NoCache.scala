package actions

import play.api.http.HeaderNames
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Custom Action composition implementation that disables client-side browser caching
  * by changing the response header of the response adding multi-browser no-cache
  * parameters. The composition can be done as follows:
  * {{{
  *
  *   def link = NoCache {
  *     deadbolt.SubjectPresent()() { implicit request =>
  *       Future {
  *         Ok(views.html.account.link(userService, auth))
  *       }
  *     }
  *   }
  *
  * }}}
  *
  * @param action The inner action
  * @tparam A Action type
  */
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
