package views.form

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Lang, MessagesApi}

case class Accept(accept: Boolean)

@Singleton
class AcceptForm @Inject() (implicit val messagesApi: MessagesApi, lang: Lang) {
  val Instance = Form {
    mapping(
      "accept" -> boolean
    )(Accept.apply)(Accept.unapply)
  }
}