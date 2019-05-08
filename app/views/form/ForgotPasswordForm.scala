package views.form

import javax.inject.Singleton

import play.api.data.Form
import play.api.data.Forms._

case class ForgotPassword(email: String)

@Singleton
class ForgotPasswordForm {
  val Instance = Form {
    mapping(
      "email" -> email
    )(ForgotPassword.apply)(ForgotPassword.unapply)
  }
}