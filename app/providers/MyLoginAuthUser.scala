package providers

import com.feth.play.module.pa.providers.password._

/**
  * For logging the user with clear password
  * @param clearPassword clear password
  * @param email user identity
  */
class MyLoginAuthUser(val clearPassword: String, val email: String) extends DefaultUsernamePasswordAuthUser(clearPassword, email) {
  //-------------------------------------------------------------------
  /**
    * For logging the user in automatically
    *
    * @param email user identity
    */
  def this(email: String) {
    this(null, email)
  }

  //-------------------------------------------------------------------
  override def expires: Long = {
    expiration
  }

  //-------------------------------------------------------------------
  // members
  //-------------------------------------------------------------------
  private val DEFAULT_SESSION_TIMEOUT: Long = 24 * 14 * 3600
  private lazy val expiration = System.currentTimeMillis + 1000 * DEFAULT_SESSION_TIMEOUT
}