package views.form

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi

case class Signup(email: String, password: String, repeatPassword: String, name: String) extends MyUsernamePassword

@Singleton
class SignupForm @Inject() (implicit val messagesApi: MessagesApi) {
  val Instance = Form {
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 5),
      "repeatPassword" -> nonEmptyText(minLength = 5),
      "name" -> nonEmptyText
    )(Signup.apply)(Signup.unapply).
      verifying(messagesApi("playauthenticate.password.signup.error.passwords_not_same"),
        data => (data.password != null) && data.password.equals(data.repeatPassword))
  }
}