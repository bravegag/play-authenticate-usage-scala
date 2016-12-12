package controllers

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import dao.UserDao
import generated.Tables.UserRow
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Controller, Flash}
import play.core.j.JavaHelpers
import providers.{MyUsernamePasswordAuthProvider, MyUsernamePasswordAuthUser}
import services.UserService
import views.account.form._

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Account @Inject() (implicit
                         val messagesApi: MessagesApi,
                         deadbolt: DeadboltActions,
                         auth: PlayAuthenticate,
                         userDao: UserDao,
                         userService: UserService,
                         authProvider: MyUsernamePasswordAuthProvider) extends Controller with I18nSupport {
  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def verifyEmail = deadbolt.Restrict(List(Array(Application.USER_ROLE_KEY)))() { request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      val Some(user: UserRow) = userService.getUser(context.session)
      val tuple =
        if (user.emailValidated.get) {
          // E-Mail has been validated already
          (Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.verify_email.error.already_validated"))
        } else
        if (user.email != null && !user.email.trim.isEmpty) {
          authProvider.sendVerifyEmailMailingAfterSignup(user, context)
          (Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.verify_email.message.instructions_sent", user.email))
        } else {
          (Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.verify_email.error.set_email_first", user.email))
        }
      Redirect(routes.Application.profile).flashing(tuple)
    }
  }

  //-------------------------------------------------------------------
  def changePassword = deadbolt.Restrict(List(Array(Application.USER_ROLE_KEY)))() { request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      val Some(user: UserRow) = userService.getUser(context.session)

      val result =
        if (!user.emailValidated.get) {
          Ok(views.html.account.unverified(userService))
        } else {
          Ok(views.html.account.password_change(userService, PasswordChangeForm.Instance))
        }
      result
    }
  }

    //-------------------------------------------------------------------
    def doChangePassword = deadbolt.Restrict(List(Array(Application.USER_ROLE_KEY)))() { implicit request =>
      Future {
        val context = JavaHelpers.createJavaContext(request)
        com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())

        val filledForm = PasswordChangeForm.Instance.bindFromRequest
        if (filledForm.hasErrors) {
          // User did not select whether to link or not link
          BadRequest(views.html.account.password_change(userService, filledForm))
        } else {
          val Some(user: UserRow) = userService.getUser(context.session)
          val newPassword = filledForm.get.password
          userService.changePassword(user, new MyUsernamePasswordAuthUser(newPassword), true)
          Redirect(routes.Application.profile).flashing(
            Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.change_password.success")
          )
        }
      }
    }
}

/**
  * Account companion object
  */
object Account