package controllers

import javax.inject._
import actions.{NoCache, SudoForbidCookieAuthAction, TryCookieAuthAction}
import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import com.nappin.play.recaptcha.{RecaptchaVerifier, WidgetHelper}
import constants.{FlashKey, SecurityRoleKey, SessionKey}
import play.api.mvc._
import services.{GoogleAuthService, UserService}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.routing.JavaScriptReverseRouter
import play.core.j.JavaHelpers
import providers.MyAuthProvider
import views.form._

@Singleton
class Application @Inject() (implicit
                             val messagesApi: MessagesApi,
                             val verifier: RecaptchaVerifier,
                             widgetHelper: WidgetHelper,
                             webJarAssets: WebJarAssets,
                             deadbolt: DeadboltActions,
                             auth: PlayAuthenticate,
                             userService: UserService,
                             authProvider: MyAuthProvider,
                             formContext: FormContext,
                             googleAuthService: GoogleAuthService) extends Controller with I18nSupport {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index =
    TryCookieAuthAction { implicit jContext =>
      deadbolt.WithAuthRequest()() { implicit request =>
        Future {
          Ok(views.html.index(userService))
        }
      }
    }

  //-------------------------------------------------------------------
  def restricted =
    TryCookieAuthAction { implicit jContext =>
      deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
        Future {
          val localUser = userService.findInSession(jContext.session)
          Ok(views.html.restricted(userService, localUser))
        }
      }
    }

  //-------------------------------------------------------------------
  def profile =
    TryCookieAuthAction { implicit jContext =>
      deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
        Future {
          val localUser = userService.findInSession(jContext.session)
          Ok(views.html.profile(auth, localUser.get, googleAuthService))
        }
      }
    }

  //-------------------------------------------------------------------
  def login =
    TryCookieAuthAction { implicit jContext =>
      deadbolt.WithAuthRequest()() { implicit request =>
        Future {
          Ok(views.html.login(auth, userService, formContext.loginForm.Instance))
        }
      }
    }

  //-------------------------------------------------------------------
  def restrictedForbidCookie =
    TryCookieAuthAction { implicit jContext =>
      SudoForbidCookieAuthAction {
        deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
          Future {
            val localUser = userService.findInSession(jContext.session)
            Ok(views.html.restrictedForbidCookie(userService, localUser))
          }
        }
      }
    }

  //-------------------------------------------------------------------
  def relogin = NoCache {
    TryCookieAuthAction { implicit jContext =>
      deadbolt.WithAuthRequest()() { implicit request =>
        Future {
          // taking chances here
          val authUser = userService.findInSession(jContext.session).get
          // partially initialize the Login form to only miss the password
          val updatedForm = formContext.loginForm.Instance.fill(views.form.Login(
            email = authUser.email.toString, password = "", isRememberMe = true))
          // everything was filled
          Ok(views.html.relogin(auth, userService, updatedForm))
        }
      }
    }
  }

  //-------------------------------------------------------------------
  def doLogin = NoCache {
    TryCookieAuthAction( implicit jContext =>
      deadbolt.WithAuthRequest()() { implicit request =>
        Future {
          formContext.loginForm.Instance.bindFromRequest.fold(
            formWithErrors => {
              // user did not fill everything properly
              BadRequest(views.html.login(auth, userService, formWithErrors))
            },
            formSuccess => {
              // everything was filled

              val result = JavaHelpers.createResult(jContext, authProvider.handleLogin(jContext, formSuccess.isRememberMe))

              def authorize(): Result =
                Option(jContext.session().remove(SessionKey.REDIRECT_TO_URI_KEY)).map { uri =>
                  result.withHeaders(LOCATION -> uri)
                }.getOrElse(result)

              auth.getUser(jContext) match {
                case null =>
                  result
                case user if googleAuthService.isEnabled(user.getId) =>
                  (formSuccess.gauthCode, formSuccess.recoveryCode) match {
                    case (Some(gauthCode), _) if googleAuthService.isValidGAuthCode(user.getId, gauthCode) =>
                      authorize()
                    case (_, Some(recoveryCode)) if googleAuthService.tryAuthenticateWithRecoveryToken(user.getId, recoveryCode) =>
                      authorize()
                    case _ =>
                      val form = formContext.loginForm.Instance.fill(formSuccess)
                      val formWithError =
                        if(formSuccess.gauthCode.isDefined) {
                          form.withGlobalError(messagesApi("playauthenticate.gauthCode.login.invalid_code"))
                        } else if(formSuccess.recoveryCode.exists(_.nonEmpty)) {
                          form.withGlobalError(messagesApi("playauthenticate.recoveryToken.login.invalid_token"))
                        } else {
                          form
                        }
                      auth.logout(jContext)
                      Ok(views.html.googleAuthentication(auth, userService, formWithError))
                  }
                case user => authorize()
              }
            }
          )
        }
    })
  }

  //-------------------------------------------------------------------
  def signup =
    TryCookieAuthAction { implicit jContext =>
      deadbolt.WithAuthRequest()() { implicit request =>
        Future {
          Ok(views.html.signup(auth, userService, formContext.signupForm.Instance))
        }
      }
    }

  //-------------------------------------------------------------------
  def doSignup =
    TryCookieAuthAction { implicit jContext =>
      NoCache {
        deadbolt.WithAuthRequest()() { implicit request =>
          verifier.bindFromRequestAndVerify(formContext.signupForm.Instance).map { form =>
            form.fold(
              formWithErrors => {
                // user did not fill everything properly
                BadRequest(views.html.signup(auth, userService, formWithErrors))
              },
              _ => {
                // everything was filled:
                // do something with your part of the form before handling the user signup
                JavaHelpers.createResult(jContext, authProvider.handleSignup(jContext))
              }
            )
          }
        }
      }
    }

  def enableGoogleAuthenticator =
    TryCookieAuthAction { implicit jContext =>
      NoCache {
        deadbolt.WithAuthRequest()() { implicit request =>
          Future {
            userService.findInSession(jContext.session) match {
              case Some(user) =>
                googleAuthService.regenerateKey(user.id)
                Ok(views.html.profile(auth, user, googleAuthService, showSecrets = true))
              case None =>
                Ok("Current user not found")
            }
          }
        }
      }
    }

  def disableGoogleAuthenticator =
    TryCookieAuthAction { implicit jContext =>
      NoCache {
        deadbolt.WithAuthRequest()() { implicit request =>
          Future {
            userService.findInSession(jContext.session) match {
              case Some(user) =>
                googleAuthService.disable(user.id)
                Redirect(routes.Application.profile)
              case None =>
                Ok("Current user not found")
            }
          }
        }
      }
    }

  //-------------------------------------------------------------------
  def jsRoutes = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      Ok(JavaScriptReverseRouter("jsRoutes")(routes.javascript.Signup.forgotPassword)).
        as("text/javascript")
    }
  }
}