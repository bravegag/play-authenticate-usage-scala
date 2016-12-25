package controllers

import javax.inject.{Inject, Singleton}

import scala.collection.mutable.ArrayBuffer
import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import constants.{FlashKey, TokenActionKey}
import play.api.mvc._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.core.j.JavaHelpers
import providers._
import services._
import generated.Tables.TokenActionRow
import views.account.signup.form._

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Signup @Inject() (implicit
                        val messagesApi: MessagesApi,
                        deadbolt: DeadboltActions,
                        auth: PlayAuthenticate,
                        userService: UserService,
                        tokenActionService: TokenActionService,
                        authProvider: MyAuthProvider,
                        forgotPasswordForm: ForgotPasswordForm,
                        passwordResetForm: PasswordResetForm) extends Controller with I18nSupport {
  import services.PluggableUserService._
  import services.PluggableTokenActionService._

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def unverified = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      Ok(views.html.account.signup.unverified(userService))
    }
  }

  //-------------------------------------------------------------------
  def forgotPassword(email: String) = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      var form = forgotPasswordForm.Instance
      if (email != null && !email.trim.isEmpty) {
        form = forgotPasswordForm.Instance.fill(ForgotPassword(email))
      }
      Ok(views.html.account.signup.password_forgot(userService, form))
    }
  }

  //-------------------------------------------------------------------
  def doForgotPassword = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      val filledForm = forgotPasswordForm.Instance.bindFromRequest
      if (filledForm.hasErrors) {
        // User did not fill in his/her email
        BadRequest(views.html.account.signup.password_forgot(userService, filledForm))
      } else {
        // The email address given *BY AN UNKNWON PERSON* to the form - we
        // should find out if we actually have a user with this email
        // address and whether password login is enabled for him/her. Also
        // only send if the email address of the user has been verified.
        val email = filledForm.get.email
        // We don't want to expose whether a given email address is signed
        // up, so just say an email has been sent, even though it might not
        // be true - that's protecting our user privacy.
        var flashValues = ArrayBuffer[(String, String)]()
        flashValues += (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.instructions_sent", email))

        val userOption = userService.findByEmail(email)
        if (userOption.isDefined) {
          val Some(user) = userOption

          // yep, we have a user with this email that is active - we do
          // not know if the user owning that account has requested this
          // reset, though.
          val provider = this.authProvider
          // User exists
          if (user.emailValidated) {
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
            flashValues += (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.email_not_verified"))

            // You might want to re-send the verification email here...
            authProvider.sendVerifyEmailMailingAfterSignup(user, context)
          }
        }
        Redirect(routes.Application.index).flashing(flashValues: _*)
      }
    }
  }

  //-------------------------------------------------------------------
  def resetPassword(token: String) = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      val ta = tokenIsValid(token, TokenActionKey.PASSWORD_RESET)
      if (ta == null) {
        BadRequest(views.html.account.signup.no_token_or_invalid(userService))

      } else {
        Ok(views.html.account.signup.password_reset(userService, passwordResetForm.Instance.fill(PasswordReset("", "", token))))
      }
    }
  }

  //-------------------------------------------------------------------
  def doResetPassword = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      val filledForm = passwordResetForm.Instance.bindFromRequest
      if (filledForm.hasErrors) {
        BadRequest(views.html.account.signup.password_reset(userService, filledForm))

      } else {
        val token = filledForm.get.token
        val newPassword = filledForm.get.password
        val option = tokenIsValid(token, TokenActionKey.PASSWORD_RESET)
        option match {
          case Some(tokenAction) => {
            var flashValues = ArrayBuffer[(String, String)]()
            val Some(user) = tokenAction.targetUser
            try {
              // Pass true for the second parameter if you want to
              // automatically create a password and the exception never to
              // happen
              user.resetPassword(new MySignupAuth(newPassword), false)
            }
            catch {
              case exception: RuntimeException => {
                flashValues += (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.no_password_account"))
              }
            }
            val login = authProvider.isLoginAfterPasswordReset
            if (login) {
              // automatically log in
              flashValues += (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.success.auto_login"))
              auth.loginAndRedirect(context, new MyLoginAuth(user.email))

            } else {
              // send the user to the login page
              flashValues += (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.success.manual_login"))
            }
            Redirect(routes.Application.login).flashing(flashValues: _*)
          }
          case None => BadRequest(views.html.account.signup.no_token_or_invalid(userService))
        }
      }
    }
  }

  //-------------------------------------------------------------------
  def oAuthDenied(getProviderKey: String) = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      Ok(views.html.account.signup.oAuthDenied(userService, getProviderKey))
    }
  }

  //-------------------------------------------------------------------
  def exists = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      Ok(views.html.account.signup.exists(userService))
    }
  }

  //-------------------------------------------------------------------
  def verify(token: String) = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
      val option = tokenIsValid(token, TokenActionKey.EMAIL_VERIFICATION)
      option match {
        case Some(tokenAction) => {
          val Some(user) =  tokenAction.targetUser
          val email = user.email
          user.verify
          val flashValues = (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.verify_email.success", email))
          if (userService.findInSession(context.session) != null) {
            Redirect(routes.Application.index).flashing(flashValues)
          } else {
            Redirect(routes.Application.login).flashing(flashValues)
          }
        }
        case None => BadRequest(views.html.account.signup.no_token_or_invalid(userService))
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
  private def tokenIsValid(token: String, `type`: TokenActionKey.Type) : Option[TokenActionRow] = {
    val result =
      if (token != null && !token.trim.isEmpty) {
        val option = tokenActionService.findByToken(token, `type`)
        option match {
          case Some(tokenAction) => {
            if (tokenAction.isValid) {
              Some(tokenAction)
            } else {
              None
            }
          }
          case None => None
        }
      } else {
        None
      }
    result
  }
}