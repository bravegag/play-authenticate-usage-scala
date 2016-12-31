package controllers

import javax.inject._

import actions.NoCache
import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import com.nappin.play.recaptcha.{RecaptchaVerifier, WidgetHelper}
import constants.SecurityRoleKey
import play.api.mvc._
import services.UserService
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.routing.JavaScriptReverseRouter
import play.core.j.JavaHelpers
import play.mvc.Http.RequestBody
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
  def doLogin = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        val jContext = JavaHelpers.createJavaContext(request.asInstanceOf[Request[RequestBody]])
        formContext.loginForm.Instance.bindFromRequest.fold(
          formWithErrors => {
            // User did not fill everything properly
            BadRequest(views.html.login(auth, userService, formWithErrors))
          },
          _ => {
            // Everything was filled
            JavaHelpers.createResult(jContext, authProvider.handleLogin(jContext))
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
            // User did not fill everything properly
            BadRequest(views.html.signup(auth, userService, formWithErrors))
          },
          _ => {
            // Everything was filled
            // do something with your part of the form before handling the user
            // signup
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