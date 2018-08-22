package helpers

import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.core.j.RequestHeaderImpl
import play.mvc._

object RequestHelpers {
  def parseRequest(request: Http.Request) = {
    request.asInstanceOf[RequestHeaderImpl]._underlyingHeader().asInstanceOf[Request[Any]].
      body.asInstanceOf[AnyContentAsFormUrlEncoded].asFormUrlEncoded.get
  }
}
