package controllers

import javax.inject._
import actions.NoCache
import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import com.feth.play.module.pa.providers.cookie.SudoForbidCookieAuthAction
import com.nappin.play.recaptcha.{RecaptchaVerifier, WidgetHelper}
import constants.SecurityRoleKey
import play.api.mvc._
import services.UserService
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.routing.JavaScriptReverseRouter
import play.core.j.JavaHelpers
import play.mvc.Http.RequestBody
import play.mvc.With
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
                             formContext: FormContext) extends Controller with I18nSupport {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      Ok(views.html.index(userService))
    }
  }

  //-------------------------------------------------------------------
  def restricted = deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
    Future {
      val jContext = JavaHelpers.createJavaContext(request)
      val localUser = userService.findInSession(jContext.session)
      Ok(views.html.restricted(userService, localUser))
    }
  }

  //-------------------------------------------------------------------
  def profile = deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
    Future {
      val jContext = JavaHelpers.createJavaContext(request)
      val localUser = userService.findInSession(jContext.session)
      Ok(views.html.profile(auth, localUser.get))
    }
  }

  //-------------------------------------------------------------------
  def login = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      Ok(views.html.login(auth, userService, formContext.loginForm.Instance))
    }
  }

  //-------------------------------------------------------------------
  //@With(classOf[SudoForbidCookieAuthAction])
  def restrictedForbidCookie = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request.asInstanceOf[Request[RequestBody]])
        val localUser = userService.findInSession(jContext.session)
        Ok(views.html.restrictedForbidCookie(userService, localUser))
      }
    }
  }

  //-------------------------------------------------------------------
  def relogin = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request.asInstanceOf[Request[RequestBody]])
        // partially initialize the Login form to only miss the password
        val updatedForm = formContext.loginForm.Instance.fill(views.form.Login(
          email = userService.findInSession(jContext.session).toString(), password = "", isRememberMe = true))
        updatedForm.bindFromRequest.fold(
          formWithErrors => {
            // user did not fill everything properly
            BadRequest(views.html.login(auth, userService, formWithErrors))
          },
          formSuccess => {
            // everything was filled
            Ok(views.html.relogin(auth, userService, formContext.loginForm.Instance))
          })
      }
    }
  }

  //-------------------------------------------------------------------
  def doLogin = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request.asInstanceOf[Request[RequestBody]])
        formContext.loginForm.Instance.bindFromRequest.fold(
          formWithErrors => {
            // user did not fill everything properly
            BadRequest(views.html.login(auth, userService, formWithErrors))
          },
          formSuccess => {
            // everything was filled
            JavaHelpers.createResult(jContext, authProvider.handleLogin(jContext, formSuccess.isRememberMe))
          }
        )
      }
    }
  }

  //-------------------------------------------------------------------
  def signup = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      Ok(views.html.signup(auth, userService, formContext.signupForm.Instance))
    }
  }

  //-------------------------------------------------------------------
  def doSignup = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      val jContext = JavaHelpers.createJavaContext(request.asInstanceOf[Request[RequestBody]])
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

  //-------------------------------------------------------------------
  def jsRoutes = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      Ok(JavaScriptReverseRouter("jsRoutes")(routes.javascript.Signup.forgotPassword)).
        as("text/javascript")
    }
  }
}