package views.account.signup.form

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, _}
import play.api.i18n.Messages

case class ForgotPassword(email: String)

@Singleton
class ForgotPasswordForm @Inject() (implicit val messages: Messages) {
  val Instance = Form {
    mapping(
      "email" -> email
    )(ForgotPassword.apply)(ForgotPassword.unapply)
  }
}