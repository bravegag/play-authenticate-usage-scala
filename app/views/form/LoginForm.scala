package views.form

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi

case class Login(email: String, password: String) extends MyUsernamePassword

@Singleton
class LoginForm @Inject() (implicit val messagesApi: MessagesApi) {
  val Instance = Form {
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 5)
    )(Login.apply)(Login.unapply)
  }
}