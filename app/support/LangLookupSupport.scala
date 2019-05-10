package support

import play.api.i18n._
import play.api.mvc._

trait LangLookupSupport {
  implicit def lang(implicit request: RequestHeader): Lang = request.acceptLanguages.head
}
