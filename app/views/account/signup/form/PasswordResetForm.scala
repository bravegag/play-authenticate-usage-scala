package views.account.signup.form

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, _}
import play.api.i18n.Messages

case class PasswordReset(password: String, repeatPassword: String, token: String)

@Singleton
class PasswordResetForm @Inject() (implicit val messages: Messages) {
  val Instance = Form {
    mapping(
      "password" -> nonEmptyText(minLength = 5),
      "repeatPassword" -> nonEmptyText(minLength = 5),
      "token" -> nonEmptyText
    )(PasswordReset.apply)(PasswordReset.unapply).
      verifying(messages("playauthenticate.change_password.error.passwords_not_same"),
        data => data.password != null && !data.password.isEmpty && data.password.equals(data.repeatPassword))
  }
}