package controllers

import javax.inject.{Inject, Singleton}

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
import views.account.form._

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Account @Inject() (implicit
                         val messagesApi: MessagesApi,
                         deadbolt: DeadboltActions,
                         auth: PlayAuthenticate,
                         userService: UserService,
                         authProvider: MyAuthProvider,
                         acceptForm: AcceptForm,
                         passwordChangeForm: PasswordChangeForm) extends Controller with I18nSupport {
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
        val Some(user: UserRow) = userService.findInSession(jContext.session)
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
        val Some(user: UserRow) = userService.findInSession(jContext.session)
        val result =
          if (!user.emailValidated) {
            Ok(views.html.account.unverified(userService))

          } else {
            Ok(views.html.account.password_change(userService, passwordChangeForm.Instance))
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
          val filledForm = passwordChangeForm.Instance.bindFromRequest
          if (filledForm.hasErrors) {
            // User did not select whether to link or not link
            BadRequest(views.html.account.password_change(userService, filledForm))

          } else {
            val Some(user: UserRow) = userService.findInSession(jContext.session)
            val newPassword = filledForm.get.password
            userService.changePassword(user, new MySignupAuthUser(newPassword), true)
            Redirect(routes.Application.profile).flashing(
              FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.change_password.success")
            )
          }
        }
      }
    }

  //-------------------------------------------------------------------
  def askLink = NoCache {
    deadbolt.SubjectPresent()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)
        val user = auth.getLinkUser(jContext.session)
        if (user == null) {
          // account to link could not be found, silently redirect to login
          Redirect(routes.Application.index)

        } else {
          Ok(views.html.account.ask_link(userService, acceptForm.Instance, user))
        }
      }
    }
  }

  //-------------------------------------------------------------------
  def doLink = NoCache {
    deadbolt.SubjectPresent()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)
        val user = auth.getLinkUser(jContext.session)
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
            val result = JavaHelpers.createResult(jContext, auth.link(jContext, link))
            link match {
              case true => result.flashing(FlashKey.FLASH_MESSAGE_KEY -> messagesApi.preferred(request)("playauthenticate.accounts.link.success"))
              case false => result
            }
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
        val userB = auth.getMergeUser(jContext.session)

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
  }

  //-------------------------------------------------------------------
  def doMerge = NoCache {
    deadbolt.SubjectPresent()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request)

        // this is the currently logged in user
        val userA = auth.getUser(jContext.session)

        // this is the user that was selected for a login
        val option = Option(auth.getMergeUser(jContext.session))
        option match {
          case Some(userB) => {
            val filledForm = acceptForm.Instance.bindFromRequest
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