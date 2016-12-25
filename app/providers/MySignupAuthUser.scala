package providers

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser
import com.feth.play.module.pa.user.NameIdentity
import views.form.Signup

/**
  * Use the default constructor for password reset only - do not use this to signup a user!
  * @param password
  * @param email
  */
class MySignupAuthUser(password: String, email: String = null) extends UsernamePasswordAuthUser(password, email) with NameIdentity {
  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  /**
    * Sign up a new user
    * @param signup form data
    */
  def this(signup: Signup) {
    this(signup.getPassword, signup.getEmail)
    name = signup.name
  }

  //-------------------------------------------------------------------
  override def getName: String = {
    return name
  }

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  private var name: String = null
}