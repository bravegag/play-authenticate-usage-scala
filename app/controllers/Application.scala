package controllers

import javax.inject._

import actions.NoCache
import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import constants.SecurityRoleKey
import play.api.mvc._
import services.UserService
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.routing.JavaScriptReverseRouter
import play.core.j.JavaHelpers
import play.mvc.Http.RequestBody
import providers.MyAuthProvider
import views.form._

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Application @Inject() (implicit
                             val messagesApi: MessagesApi,
                             deadbolt: DeadboltActions,
                             auth: PlayAuthenticate,
                             userService: UserService,
                             authProvider: MyAuthProvider,
                             loginForm: LoginForm,
                             signupForm: SignupForm) extends Controller with I18nSupport {

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
      val context = JavaHelpers.createJavaContext(request)
      val localUser = userService.findInSession(context.session)
      Ok(views.html.restricted(userService, localUser))
    }
  }

  //-------------------------------------------------------------------
  def profile = deadbolt.Restrict(List(Array(SecurityRoleKey.USER_ROLE.toString)))() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      val localUser = userService.findInSession(context.session)
      Ok(views.html.profile(auth, localUser.get))
    }
  }

  //-------------------------------------------------------------------
  def login = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      Ok(views.html.login(auth, userService, loginForm.Instance))
    }
  }

  //-------------------------------------------------------------------
  def doLogin = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        val context = JavaHelpers.createJavaContext(request.asInstanceOf[Request[RequestBody]])
        val filledForm = loginForm.Instance.bindFromRequest
        if (filledForm.hasErrors) {
          // User did not fill everything properly
          BadRequest(views.html.login(auth, userService, filledForm))
        }
        else {
          // Everything was filled
          JavaHelpers.createResult(context, authProvider.handleLogin(context))
        }
      }
    }
  }

  //-------------------------------------------------------------------
  def signup = deadbolt.WithAuthRequest()() { implicit request =>
    Future {
      Ok(views.html.signup(auth, userService, signupForm.Instance))
    }
  }

  //-------------------------------------------------------------------
  def doSignup = NoCache {
    deadbolt.WithAuthRequest()() { implicit request =>
      Future {
        val context = JavaHelpers.createJavaContext(request.asInstanceOf[Request[RequestBody]])
        val filledForm = signupForm.Instance.bindFromRequest
        if (filledForm.hasErrors) {
          // User did not fill everything properly
          BadRequest(views.html.signup(auth, userService, filledForm))

        } else {
          // Everything was filled
          // do something with your part of the form before handling the user
          // signup
          JavaHelpers.createResult(context, authProvider.handleSignup(context))
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