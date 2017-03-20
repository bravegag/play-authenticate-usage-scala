package controllers

import javax.inject._

import actions.NoCache

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
import views.form._

@Singleton
class Signup @Inject() (implicit
                        val messagesApi: MessagesApi,
                        webJarAssets: WebJarAssets,
                        deadbolt: DeadboltActions,
                        auth: PlayAuthenticate,
                        userService: UserService,
                        tokenActionService: TokenActionService,
                        authProvider: MyAuthProvider,
                        formContext: FormContext) extends Controller with I18nSupport {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import services.PluggableUserService._
  import services.PluggableTokenActionService._

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def unverified = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        Ok(views.html.account.signup.unverified(userService))
      }
    }
  }

  //-------------------------------------------------------------------
  def forgotPassword(email: String) = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        val form = Option(email) match {
          case Some(email) => {
            if (!email.trim.isEmpty) {
              formContext.forgotPasswordForm.Instance.fill(ForgotPassword(email))

            } else {
              formContext.forgotPasswordForm.Instance
            }
          }
          case None => {
            formContext.forgotPasswordForm.Instance
          }
        }
        Ok(views.html.account.signup.password_forgot(userService, form))
      }
    }
  }

  //-------------------------------------------------------------------
  def doForgotPassword = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)
        formContext.forgotPasswordForm.Instance.bindFromRequest.fold(
          formWithErrors => {
            // User did not fill in his/her email
            BadRequest(views.html.account.signup.password_forgot(userService, formWithErrors))
          },
          formSuccess => {
            // The email address given *BY AN UNKNWON PERSON* to the form - we
            // should find out if we actually have a user with this email
            // address and whether password login is enabled for him/her. Also
            // only send if the email address of the user has been verified.
            val email = formSuccess.email
            // We don't want to expose whether a given email address is signed
            // up, so just say an email has been sent, even though it might not
            // be true - that's protecting our user privacy.
            var flashValues = ArrayBuffer[(String, String)]()
            flashValues += (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.reset_password.message.instructions_sent", email))

            val userOption = userService.findByEmail(email)
            userOption.map { user =>
              // yep, we have a user with this email that is active - we do
              // not know if the user owning that account has requested this
              // reset, though.
              // User exists
              if (user.emailValidated) {
                authProvider.sendPasswordResetMailing(user, jContext)
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
                authProvider.sendVerifyEmailMailingAfterSignup(user, jContext)
              }
            }
            Redirect(routes.Application.index).flashing(flashValues: _*)
          }
        )
      }
    }
  }

  //-------------------------------------------------------------------
  def resetPassword(token: String) = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        tokenIsValid(token, TokenActionKey.PASSWORD_RESET) match {
          case Some(_) => Ok(views.html.account.signup.password_reset(userService,
            formContext.passwordResetForm.Instance.fill(PasswordReset("", "", token))))
          case None => BadRequest(views.html.account.signup.no_token_or_invalid(userService))
        }
      }
    }
  }

  //-------------------------------------------------------------------
  def doResetPassword = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)
        formContext.passwordResetForm.Instance.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.account.signup.password_reset(userService, formWithErrors)),
          formSuccess => {
            val token = formSuccess.token
            val newPassword = formSuccess.password
            tokenIsValid(token, TokenActionKey.PASSWORD_RESET) match {
              case Some(tokenAction) => {
                var flashValues = ArrayBuffer[(String, String)]()
                val Some(user) = tokenAction.targetUser
                try {
                  // Pass true for the second parameter if you want to
                  // automatically create a password and the exception never to
                  // happen
                  user.resetPassword(new MySignupAuthUser(newPassword), false)
                }
                catch {
                  case _ : RuntimeException => {
                    flashValues += (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.
                      preferred(request)("playauthenticate.reset_password.message.no_password_account"))
                  }
                }
                val login = authProvider.isLoginAfterPasswordReset
                if (login) {
                  // automatically log in
                  flashValues += (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.
                    preferred(request)("playauthenticate.reset_password.message.success.auto_login"))
                  auth.loginAndRedirect(jContext, new MyLoginAuthUser(user.email))

                } else {
                  // send the user to the login page
                  flashValues += (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.
                    preferred(request)("playauthenticate.reset_password.message.success.manual_login"))
                }
                Redirect(routes.Application.login).flashing(flashValues: _*)
              }
              case None => BadRequest(views.html.account.signup.no_token_or_invalid(userService))
            }
          }
        )
      }
    }
  }

  //-------------------------------------------------------------------
  def oAuthDenied(getProviderKey: String) = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        Ok(views.html.account.signup.oAuthDenied(userService, getProviderKey))
      }
    }
  }

  //-------------------------------------------------------------------
  def exists = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        Ok(views.html.account.signup.exists(userService))
      }
    }
  }

  //-------------------------------------------------------------------
  def verify(token: String) = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)
        tokenIsValid(token, TokenActionKey.EMAIL_VERIFICATION) match {
          case Some(tokenAction) => {
            val Some(user) =  tokenAction.targetUser
            val email = user.email
            user.verify
            val flashValues = (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.
              preferred(request)("playauthenticate.verify_email.success", email))
            userService.findInSession(jContext.session) match {
              case Some(_) => Redirect(routes.Application.index).flashing(flashValues)
              case None => Redirect(routes.Application.login).flashing(flashValues)
            }
          }
          case None => BadRequest(views.html.account.signup.no_token_or_invalid(userService))
        }
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
        tokenActionService.findByToken(token, `type`) match {
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