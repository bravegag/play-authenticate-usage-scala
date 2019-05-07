package controllers

import javax.inject._
import actions.{NoCache, SudoForbidCookieAuthAction, TryCookieAuthAction}
import akka.stream.Materializer
import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import com.nappin.play.recaptcha.{RecaptchaVerifier, WidgetHelper}
import constants.{SecurityRoleKey, SessionKey}
import org.webjars.play._
import play.api.{Configuration, Environment}
import play.api.mvc._
import services.{GoogleAuthService, UserService}
import play.api.i18n._
import play.api.routing.JavaScriptReverseRouter
import play.core.j.JavaHelpers
import providers.MyAuthProvider
import views.form._
import views.html.recaptcha

@Singleton
class Application @Inject() (implicit
                             val verifier: RecaptchaVerifier,
                             config: Configuration,
                             env: Environment,
                             mat: Materializer,
                             lang: Lang,
                             indexView: views.html.index,
                             restrictedView: views.html.restricted,
                             profileView: views.html.profile,
                             loginView: views.html.login,
                             restrictedForbidCookieView: views.html.restricted_forbid_cookie,
                             reloginView: views.html.relogin,
                             googleAuthenticationView: views.html.google_authentication,
                             signupView: views.html.signup,
                             controllerComponents: ControllerComponents,
                             widgetHelper: WidgetHelper,
                             webJarUtil: WebJarsUtil,
                             deadbolt: DeadboltActions,
                             auth: PlayAuthenticate,
                             userService: UserService,
                             authProvider: MyAuthProvider,
                             formContext: FormContext,
                             googleAuthService: GoogleAuthService,
                             recaptchaWidget: recaptcha.recaptchaWidget) extends AbstractController(controllerComponents) with I18nSupport {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index =
    TryCookieAuthAction { implicit jContext =>
      deadbolt.WithAuthRequest()() { implicit request =>
        Future {
          Ok(indexView(userService))
        }
      }
    }

  //-------------------------------------------------------------------
  def restricted =
    TryCookieAuthAction { implicit jContext =>
      deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
        Future {
          val localUser = userService.findInSession(jContext.session)
          Ok(restrictedView(userService, localUser))
        }
      }
    }

  //-------------------------------------------------------------------
  def profile =
    TryCookieAuthAction { implicit jContext =>
      deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
        Future {
          val localUser = userService.findInSession(jContext.session)
          Ok(profileView(auth, localUser.get, googleAuthService))
        }
      }
    }

  //-------------------------------------------------------------------
  def login =
    TryCookieAuthAction { implicit jContext =>
      deadbolt.WithAuthRequest()() { implicit request =>
        Future {
          Ok(loginView(auth, userService, formContext.loginForm.Instance))
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
            Ok(restrictedForbidCookieView(userService, localUser))
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
          Ok(reloginView(auth, userService, updatedForm))
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
              BadRequest(loginView(auth, userService, formWithErrors))
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
                      Ok(googleAuthenticationView(auth, userService, formWithError))
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
          Ok(signupView(auth, userService, formContext.signupForm.Instance))
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
                BadRequest(signupView(auth, userService, formWithErrors))
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
                Ok(profileView(auth, user, googleAuthService, showSecrets = true))
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