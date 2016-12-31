package views.form

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi

case class PasswordReset(password: String, repeatPassword: String, token: String)

@Singleton
class PasswordResetForm @Inject() (implicit val messagesApi: MessagesApi) {
  val Instance = Form {
    mapping(
      "password" -> nonEmptyText(minLength = 5),
      "repeatPassword" -> nonEmptyText(minLength = 5),
      "token" -> nonEmptyText
    )(PasswordReset.apply)(PasswordReset.unapply).
      verifying(messagesApi("playauthenticate.change_password.error.passwords_not_same"),
        data => data.password != null && !data.password.isEmpty && data.password.equals(data.repeatPassword))
  }
}