package views.form

import javax.inject.Singleton
import play.api.data.Form
import play.api.data.Forms._

case class Login(email: String, password: String, isRememberMe: Boolean, gauthCode: Option[Int] = None, recoveryCode: Option[String] = None) extends MyUsernamePassword

@Singleton
class LoginForm {
  val Instance = Form {
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 5),
      "rememberMe" -> boolean,
      "gauthCode" -> optional(number),
      "recoveryToken" -> optional(text)
    )(Login.apply)(Login.unapply)
  }
}