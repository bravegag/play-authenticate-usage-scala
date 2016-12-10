package service

import com.feth.play.module.pa.Resolver
import com.feth.play.module.pa.exceptions.AccessDeniedException
import com.feth.play.module.pa.exceptions.AuthException
import controllers.routes
import play.mvc.Call

class MyResolver extends Resolver {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def login: Call = {
    // Your login page
    // TODO: migrate
    //routes.Application.login
    routes.Application.index
  }

  //------------------------------------------------------------------------
  def afterAuth: Call = {
    // The user will be redirected to this page after authentication
    // if no original URL was saved
    routes.Application.index
  }

  //------------------------------------------------------------------------
  def afterLogout: Call = routes.Application.index

  //------------------------------------------------------------------------
  def auth(provider: String): Call = {
    // You can provide your own authentication implementation,
    // however the default should be sufficient for most cases
    // TODO: migrate
    //com.feth.play.module.pa.controllers.routes.Authenticate.authenticate(provider)
    routes.Application.index
  }

  //------------------------------------------------------------------------
  def askMerge: Call = {
    // TODO: migrate
    //routes.Account.askMerge
    routes.Application.index
  }

  //------------------------------------------------------------------------
  def askLink: Call = {
    // TODO: migrate
    //routes.Account.askLink
    routes.Application.index
  }

  //------------------------------------------------------------------------
  override def onException(e: AuthException): Call = {
    if (e.isInstanceOf[AccessDeniedException]) {
      // TODO: migrate
      //routes.Signup.oAuthDenied(e.asInstanceOf[AccessDeniedException].getProviderKey)
      routes.Application.index
    }
    // more custom problem handling here...
    super.onException(e)
  }
}