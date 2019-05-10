package views.form

import javax.inject.Singleton
import play.api.data.Form
import play.api.data.Forms._

case class Accept(accept: Boolean)

@Singleton
class AcceptForm {
  val Instance = Form {
    mapping(
      "accept" -> boolean
    )(Accept.apply)(Accept.unapply)
  }
}