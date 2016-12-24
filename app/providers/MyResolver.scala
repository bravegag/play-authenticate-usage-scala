package providers

import com.feth.play.module.pa.Resolver
import com.feth.play.module.pa.controllers.routes.Authenticate
import com.feth.play.module.pa.exceptions.{AccessDeniedException, AuthException}
import controllers.routes
import play.mvc.Call

class MyResolver extends Resolver {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  override def login: Call = {
    // Your login page
    routes.Application.login
  }

  //------------------------------------------------------------------------
  override def afterAuth: Call = {
    // The user will be redirected to this page after authentication
    // if no original URL was saved
    routes.Application.index
  }

  //------------------------------------------------------------------------
  override def afterLogout: Call = {
    routes.Application.index
  }

  //------------------------------------------------------------------------
  override def auth(provider: String): Call = {
    // You can provide your own authentication implementation,
    // however the default should be sufficient for most cases
    Authenticate.authenticate(provider)
  }

  //------------------------------------------------------------------------
  override def askMerge: Call = {
    routes.Account.askMerge
  }

  //------------------------------------------------------------------------
  override def askLink: Call = {
    routes.Account.askLink
  }

  //------------------------------------------------------------------------
  override def onException(authException: AuthException): Call = {
    authException match {
      case accessDeniedException: AccessDeniedException =>
        routes.Signup.oAuthDenied(accessDeniedException.getProviderKey)
      case _ => // ignore
    }

    // more custom problem handling here...
    super.onException(authException)
  }
}