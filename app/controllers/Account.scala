package controllers

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import dao.UserDao
import generated.Tables.UserRow
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Controller, Flash}
import play.data.FormFactory
import services.UserService

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Account @Inject() (implicit
                         val messagesApi: MessagesApi,
                         deadbolt: DeadboltActions,
                         auth: PlayAuthenticate,
                         userProvider: UserService,
                         userDao: UserDao,
                         formFactory: FormFactory) extends Controller with I18nSupport {
  import utils.PlayConversions._

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def verifyEmail = deadbolt.Restrict(List(Array(ApplicationKeys.UserRole)))() { request =>
    Future {
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(response)
      val Some(user: UserRow) = userProvider.getUser(request.session)
      val tuple =
        if (user.emailValidated.get) {
          // E-Mail has been validated already
          (ApplicationKeys.FlashMessage -> messagesApi.preferred(request)("playauthenticate.verify_email.error.already_validated"))
        } else
        if (user.email != null && !user.email.trim.isEmpty) {
          myUsrPaswProvider.sendVerifyEmailMailingAfterSignup(user, ctx)
          (ApplicationKeys.FlashMessage -> messagesApi.preferred(request)("playauthenticate.verify_email.message.instructions_sent", user.email))
        } else {
          (ApplicationKeys.FlashMessage -> messagesApi.preferred(request)("playauthenticate.verify_email.error.set_email_first", user.email))
        }
      Redirect(routes.Application.profile).flashing(tuple)
    }
  }
}
