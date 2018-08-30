package views.form

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi

case class Login(email: String, password: String, isRememberMe: Boolean, gauthCode: Option[Int] = None, recoveryCode: Option[String] = None) extends MyUsernamePassword

@Singleton
class LoginForm @Inject() (implicit val messagesApi: MessagesApi) {
  val Instance = Form {
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 5),
      "rememberMe" -> boolean,
      "gauthCode" -> optional(number),
      "recoveryCode" -> optional(text)
    )(Login.apply)(Login.unapply)
  }
}