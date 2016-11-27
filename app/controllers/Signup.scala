package controllers

import play.api.mvc._

class Signup extends Controller {
  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def forgotPassword(email: String) = Action {
    // TODO: migrate
/*
    com.feth.play.module.pa.controllers.Authenticate.noCache(response)
    var form = FORGOT_PASSWORD_FORM
    if (email != null && !email.trim.isEmpty) form = FORGOT_PASSWORD_FORM.fill(new MyUsernamePasswordAuthProvider.MyIdentity(email))
    Ok(password_forgot.render(this.userProvider, form))
*/
    Ok("TODO: password forgot")
  }

  //-------------------------------------------------------------------
  // members
  //-------------------------------------------------------------------
  private val FORGOT_PASSWORD_FORM = null
}
