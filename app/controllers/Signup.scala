package controllers

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import dao.UserDao
import play.api.mvc._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.core.j.JavaHelpers
import providers.{MyLoginUsernamePasswordAuthUser, MyUsernamePasswordAuthProvider, MyUsernamePasswordAuthUser}
import services.UserService
import play.api.data.Form
import play.api.data.Forms._
import play.mvc.Result
import views.html.account.signup.{exists, oAuthDenied}

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Signup @Inject() (implicit
                        val messagesApi: MessagesApi,
                        deadbolt: DeadboltActions,
                        auth: PlayAuthenticate,
                        userService: UserService,
                        userDao: UserDao,
                        authProvider: MyUsernamePasswordAuthProvider) extends Controller with I18nSupport {
  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def unverified = Action { request =>
    val context = JavaHelpers.createJavaContext(request)
    com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
    Ok(views.html.account.signup.unverified(userService))
  }

  //-------------------------------------------------------------------
  def forgotPassword(email: String) = Action { request =>
    val context = JavaHelpers.createJavaContext(request)
    com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
    var form = FORGOT_PASSWORD_FORM
    if (email != null && !email.trim.isEmpty) {
      form = FORGOT_PASSWORD_FORM.fill(new MyUsernamePasswordAuthProvider.MyIdentity(email))
    }
    Ok(views.html.account.signup.password_forgot(userService, form))
  }

  //-------------------------------------------------------------------
  def doForgotPassword = Action { request =>
    val context = JavaHelpers.createJavaContext(request)
    com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
    val filledForm = FORGOT_PASSWORD_FORM.bindFromRequest
    if (filledForm.hasErrors) {
      // User did not fill in his/her email
      BadRequest(views.html.account.signup.password_forgot(userService, filledForm))
    }
    else {
      // The email address given *BY AN UNKNWON PERSON* to the form - we
      // should find out if we actually have a user with this email
      // address and whether password login is enabled for him/her. Also
      // only send if the email address of the user has been verified.
      val email = filledForm.get.email
      // We don't want to expose whether a given email address is signed
      // up, so just say an email has been sent, even though it might not
      // be true - that's protecting our user privacy.
      flash(Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.instructions_sent", email))

      // TODO: handle Option correctly
      val Some(user) = userService.findByEmail(email)
      if (user != null) {
        // yep, we have a user with this email that is active - we do
        // not know if the user owning that account has requested this
        // reset, though.
        val provider = this.authProvider
        // User exists
        if (user.emailValidated.get) {
          provider.sendPasswordResetMailing(user, context)
          // In case you actually want to let (the unknown person)
          // know whether a user was found/an email was sent, use,
          // change the flash message
        } else {
          // We need to change the message here, otherwise the user
          // does not understand whats going on - we should not verify
          // with the password reset, as a "bad" user could then sign
          // up with a fake email via OAuth and get it verified by an
          // a unsuspecting user that clicks the link.
          flash(Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.email_not_verified"))

          // You might want to re-send the verification email here...
          provider.sendVerifyEmailMailingAfterSignup(user, context)
        }
      }
      Redirect(routes.Application.index)
    }
  }

  //-------------------------------------------------------------------
  def resetPassword(token: String) = Action { request =>
    val context = JavaHelpers.createJavaContext(request)
    com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
    val ta = tokenIsValid(token, Type.PASSWORD_RESET)
    if (ta == null) {
      BadRequest(views.html.account.signup.no_token_or_invalid(userService))

    } else {
      Ok(views.html.account.signup.password_reset(userService, PASSWORD_RESET_FORM.fill(new Signup.PasswordReset(token))))
    }
  }

  //-------------------------------------------------------------------
  def doResetPassword = Action { request =>
    val context = JavaHelpers.createJavaContext(request)
    com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
    val filledForm = PASSWORD_RESET_FORM.bindFromRequest
    if (filledForm.hasErrors) {
      BadRequest(views.html.account.signup.password_reset(userService, filledForm))

    } else {
      val token = filledForm.get.token
      val newPassword = filledForm.get.password
      val ta = tokenIsValid(token, Type.PASSWORD_RESET)
      if (ta == null) {
        BadRequest(views.html.account.signup.no_token_or_invalid(userService))
      } else {
        val u = ta.targetUser
        try
          // Pass true for the second parameter if you want to
          // automatically create a password and the exception never to
          // happen
          u.resetPassword(new MyUsernamePasswordAuthUser(newPassword), false)

        catch {
          case re: RuntimeException => {
            flash(Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.no_password_account"))
          }
        }
        val login = authProvider.isLoginAfterPasswordReset
        if (login) {
          // automatically log in
          flash(Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.success.auto_login"))
          auth.loginAndRedirect(context, new MyLoginUsernamePasswordAuthUser(u.email))

        } else {
          // send the user to the login page
          flash(Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.success.manual_login"))
        }
        Redirect(routes.Application.login)
      }
    }
  }

  //-------------------------------------------------------------------
  def oAuthDenied(getProviderKey: String) = Action { request =>
    val context = JavaHelpers.createJavaContext(request)
    com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
    Ok(views.html.account.signup.oAuthDenied(userService, getProviderKey))
  }

  //-------------------------------------------------------------------
  def exists = Action { request =>
    val context = JavaHelpers.createJavaContext(request)
    com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
    Ok(views.html.account.signup.exists(userService))
  }

  //-------------------------------------------------------------------
  def verify(token: String) = Action { request =>
    val context = JavaHelpers.createJavaContext(request)
    com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
    val ta = tokenIsValid(token, Type.EMAIL_VERIFICATION)
    if (ta == null) {
      BadRequest(views.html.account.signup.no_token_or_invalid(userService))
    } else {
      val email = ta.targetUser.email
      User.verify(ta.targetUser)
      flash(Application.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.verify_email.success", email))
      if (userService.getUser(context.session) != null) {
        Redirect(routes.Application.index)
      } else {
        Redirect(routes.Application.login)
      }
    }
  }

  //-------------------------------------------------------------------
  // private
  //-------------------------------------------------------------------
  /**
    * Returns a token object if valid, null otherwise
    * @param token
    * @param type
    * @return a token object if valid, null otherwise
    */
  private def tokenIsValid(token: String, `type`: TokenAction.Type) = {
    var result = null
    if (token != null && !token.trim.isEmpty) {
      val ta = TokenAction.findByToken(token, `type`)
      if (ta != null && ta.isValid) {
        result = ta
      }
    }
    result
  }

  //-------------------------------------------------------------------
  // members
  //-------------------------------------------------------------------
  private val FORGOT_PASSWORD_FORM : Form[Any] = null
  private val FORGOT_PASSWORD_FORM : Form[Any] = null
}

/**
  * Signup companion object
  */
object Signup

