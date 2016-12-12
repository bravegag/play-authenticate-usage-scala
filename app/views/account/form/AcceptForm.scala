package views.account.form

import javax.inject.{Inject, Singleton}

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

case class Accept(accept: Boolean)

@Singleton
class AcceptForm @Inject() (implicit val messages: Messages) {
  val Instance = Form {
    mapping(
      "accept" -> boolean
    )(Accept.apply)(Accept.unapply)
  }
}