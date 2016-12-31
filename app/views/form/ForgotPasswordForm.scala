package views.form

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi

case class ForgotPassword(email: String)

@Singleton
class ForgotPasswordForm @Inject() (implicit val messagesApi: MessagesApi) {
  val Instance = Form {
    mapping(
      "email" -> email
    )(ForgotPassword.apply)(ForgotPassword.unapply)
  }
}