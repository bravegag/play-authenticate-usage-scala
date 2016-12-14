package views.form

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

case class Signup(email: String, password: String, repeatPassword: String, username: String) extends MyUsernamePassword

@Singleton
class SignupForm @Inject() (implicit val messages: Messages) {
  val Instance = Form {
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 5),
      "repeatPassword" -> nonEmptyText(minLength = 5),
      "username" -> nonEmptyText()
    )(Signup.apply)(Signup.unapply).
      verifying(messages("playauthenticate.password.signup.error.passwords_not_same"),
        data => data.password != null && !data.password.isEmpty && data.password.equals(data.repeatPassword))
  }
}