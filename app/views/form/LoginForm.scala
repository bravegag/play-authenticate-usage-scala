package views.form

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

case class Login(email: String, password: String) extends MyUsernamePassword

@Singleton
class LoginForm @Inject() (implicit val messages: Messages) {
  val Instance = Form {
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 5)
    )(Login.apply)(Login.unapply)
  }
}