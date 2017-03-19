package controllers

import javax.inject._

import actions.NoCache
import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import constants.{FlashKey, SecurityRoleKey}
import generated.Tables.UserRow
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.core.j.JavaHelpers
import providers.{MyAuthProvider, MySignupAuthUser}
import services.UserService
import views.form._

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Account @Inject() (implicit
                         val messagesApi: MessagesApi,
                         webJarAssets: WebJarAssets,
                         deadbolt: DeadboltActions,
                         auth: PlayAuthenticate,
                         userService: UserService,
                         authProvider: MyAuthProvider,
                         formContext: FormContext) extends Controller with I18nSupport {
  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def link = NoCache {
    deadbolt.SubjectPresent()() { implicit request =>
      Future {
        Ok(views.html.account.link(userService, auth))
      }
    }
  }

  //-------------------------------------------------------------------
  def verifyEmail = NoCache {
    deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)
        // TODO: change because this is cowboy style
        val Some(user) = userService.findInSession(jContext.session)
        val tuple =
          if (user.emailValidated) {
            // email has been validated already
            (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.verify_email.error.already_validated"))
          } else
          if (user.email != null && !user.email.trim.isEmpty) {
            authProvider.sendVerifyEmailMailingAfterSignup(user, jContext)
            (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.verify_email.message.instructions_sent", user.email))
          } else {
            (FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.verify_email.error.set_email_first", user.email))
          }

        Redirect(routes.Application.profile).flashing(tuple)
      }
    }
  }

  //-------------------------------------------------------------------
  def changePassword = NoCache {
    deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)
        // TODO: change because this is cowboy style
        val Some(user) = userService.findInSession(jContext.session)
        val result =
          if (!user.emailValidated) {
            Ok(views.html.account.unverified(userService))

          } else {
            Ok(views.html.account.password_change(userService, formContext.passwordChangeForm.Instance))
          }
        result
      }
    }
  }

    //-------------------------------------------------------------------
    def doChangePassword = NoCache {
      deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
        Future {
          val jContext = JavaHelpers.createJavaContext(request)
          formContext.passwordChangeForm.Instance.bindFromRequest.fold(
            formWithErrors => {
              // User did not select whether to link or not link
              BadRequest(views.html.account.password_change(userService, formWithErrors))
            },
            formSuccess => {
              val Some(user: UserRow) = userService.findInSession(jContext.session)
              val newPassword = formSuccess.password
              userService.changePassword(user, new MySignupAuthUser(newPassword), true)
              Redirect(routes.Application.profile).flashing(
                FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.change_password.success")
              )
          })
        }
      }
    }

  //-------------------------------------------------------------------
  def askLink = NoCache {
    deadbolt.SubjectPresent()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)
        Option(auth.getLinkUser(jContext.session)) match {
          case Some(user) => Ok(views.html.account.ask_link(userService, formContext.acceptForm.Instance, user))
          case None => {
            // account to link could not be found, silently redirect to login
            Redirect(routes.Application.index)
          }
        }
      }
    }
  }

  //-------------------------------------------------------------------
  def doLink = NoCache {
    deadbolt.SubjectPresent()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)
        Option(auth.getLinkUser(jContext.session)) match {
          case Some(user) => {
            formContext.acceptForm.Instance.bindFromRequest.fold(
              formWithErrors => BadRequest(views.html.account.ask_link(userService, formWithErrors, user)),
              formSuccess => {
                // User made a choice :)
                val link = formSuccess.accept
                val result = JavaHelpers.createResult(jContext, auth.link(jContext, link))
                link match {
                  case true => result.flashing(FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.accounts.link.success"))
                  case false => result
                }
              }
            )
          }
          case None  => {
            // account to link could not be found, silently redirect to login
            Redirect(routes.Application.index)
          }
        }
      }
    }
  }

  //-------------------------------------------------------------------
  def askMerge = NoCache {
    deadbolt.SubjectPresent()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)

        // this is the currently logged in user
        val userA = auth.getUser(jContext.session)

        // this is the user that was selected for a login
        Option(auth.getMergeUser(jContext.session)) match {
          case Some(userB) => {
            // You could also get the local user object here via
            // User.findByAuthUserIdentity(newUser)
            Ok(views.html.account.ask_merge(userService, formContext.acceptForm.Instance, userA, userB))
          }
          case None => {
            // user to merge with could not be found, silently redirect to login
            Redirect(routes.Application.index)
          }
        }
      }
    }
  }

  //-------------------------------------------------------------------
  def doMerge = NoCache {
    deadbolt.SubjectPresent()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)

        // this is the currently logged in user
        val userA = auth.getUser(jContext.session)

        // this is the user that was selected for a login
        Option(auth.getMergeUser(jContext.session)) match {
          case Some(userB) => {
            val filledForm = formContext.acceptForm.Instance.bindFromRequest
            if (filledForm.hasErrors) {
              // User did not select whether to merge or not merge
              BadRequest(views.html.account.ask_merge(userService, filledForm, userA, userB))

            } else {
              // User made a choice :)
              val merge = filledForm.get.accept
              val result = JavaHelpers.createResult(jContext, auth.merge(jContext, merge))
              merge match {
                case true => result.flashing(FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.accounts.merge.success"))
                case false => result
              }
            }
          }
          case None => {
            // user to merge with could not be found, silently redirect to login
            Redirect(routes.Application.index)
          }
        }
      }
    }
  }
}