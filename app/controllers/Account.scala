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
import views.html.account._

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Account @Inject() (implicit
                         val messagesApi: MessagesApi,
                         deadbolt: DeadboltActions,
                         auth: PlayAuthenticate,
                         userDao: UserDao,
                         userService: UserService,
                         authProvider: MyUsernamePasswordAuthProvider,
                         acceptForm: AcceptForm,
                         passwordChangeForm: PasswordChangeForm) extends Controller with I18nSupport {
  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def link = deadbolt.SubjectPresent()() { request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      Ok(views.html.account.link(userService, auth))
    }
  }

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
          Ok(views.html.account.password_change(userService, passwordChangeForm.Instance))
        }
      result
    }
  }

    //-------------------------------------------------------------------
    def doChangePassword = deadbolt.Restrict(List(Array(Application.USER_ROLE_KEY)))() { implicit request =>
      Future {
        val context = JavaHelpers.createJavaContext(request)
        com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())

        val filledForm = passwordChangeForm.Instance.bindFromRequest
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

  //-------------------------------------------------------------------
  def askLink = deadbolt.SubjectPresent()() { request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      val user = this.auth.getLinkUser(context.session)
      if (user == null) {
        // account to link could not be found, silently redirect to login
        Redirect(routes.Application.index)

      } else {
        Ok(views.html.account.ask_link(userService, acceptForm.Instance, user))
      }
    }
  }

  //-------------------------------------------------------------------
  def doLink = deadbolt.SubjectPresent()() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      val user = this.auth.getLinkUser(context.session)
      if (user == null) {
        // account to link could not be found, silently redirect to login
        Redirect(routes.Application.index)

      } else {
        val filledForm = acceptForm.Instance.bindFromRequest
        if (filledForm.hasErrors) {
          BadRequest(views.html.account.ask_link(userService, filledForm, user))

        } else {
          // User made a choice :)
          val link = filledForm.get.accept
          val result = JavaHelpers.createResult(context, auth.link(context, link))
          link match {
            case true => result.flashing(Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.accounts.link.success"))
            case false => result
          }
        }
      }
    }
  }

  //-------------------------------------------------------------------
  def askMerge = deadbolt.SubjectPresent()() { request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())

      // this is the currently logged in user
      val userA = auth.getUser(context.session)

      // this is the user that was selected for a login
      val userB = auth.getMergeUser(context.session)

      if (userB == null) {
        // user to merge with could not be found, silently redirect to login
        Redirect(routes.Application.index)

      } else {
        // You could also get the local user object here via
        // User.findByAuthUserIdentity(newUser)
        Ok(views.html.account.ask_merge(userService, acceptForm.Instance, userA, userB))
      }
    }
  }

  //-------------------------------------------------------------------
  def doMerge = deadbolt.SubjectPresent()() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())

      // this is the currently logged in user
      val userA = auth.getUser(context.session)

      // this is the user that was selected for a login
      val userB = auth.getMergeUser(context.session)

      if (userB == null) {
        // user to merge with could not be found, silently redirect to login
        Redirect(routes.Application.index)

      } else {
        val filledForm = acceptForm.Instance.bindFromRequest
        if (filledForm.hasErrors) {
          // User did not select whether to merge or not merge
          BadRequest(views.html.account.ask_merge(userService, filledForm, userA, userB))

        } else {
          // User made a choice :)
          val merge = filledForm.get.accept
          val result = JavaHelpers.createResult(context, auth.merge(context, merge))
          merge match {
            case true => result.flashing(Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.accounts.merge.success"))
            case false => result
          }
        }
      }
    }
  }
}

/**
  * Account companion object
  */
object Account