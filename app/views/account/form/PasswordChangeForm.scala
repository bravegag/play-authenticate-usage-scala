package views.account.form

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.i18n.Messages

case class PasswordChange(password: String, repeatPassword: String)

object PasswordChangeForm {
  val Instance = Form {
    mapping(
      "password" -> text(minLength = 5),
      "repeatPassword" -> text(minLength = 5)
    )(PasswordChange.apply)(PasswordChange.unapply).
      verifying(Messages("playauthenticate.change_password.error.passwords_not_same"),
        data => data.password != null && !data.password.isEmpty && data.password.equals(data.repeatPassword))
  }
}