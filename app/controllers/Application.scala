package controllers

import javax.inject._

import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc._
import services.UserService
import dao.UserDao
import play.api.i18n.{I18nSupport, MessagesApi}
import play.core.j.JavaHelpers
import providers.MyUsernamePasswordAuthProvider

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Application @Inject() (implicit
                             val messagesApi: MessagesApi,
                             session: Session,
                             deadbolt: DeadboltActions,
                             auth: PlayAuthenticate,
                             userService: UserService,
                             userDao: UserDao,
                             authProvider: MyUsernamePasswordAuthProvider) extends Controller with I18nSupport {

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index = Action { implicit request =>
    Ok(views.html.index(userService))
  }

  //-------------------------------------------------------------------
  def restricted = deadbolt.Restrict(List(Array(Application.USER_ROLE_KEY)))() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      val localUser = userService.getUser(context.session)
      Ok(views.html.restricted(userService, localUser))
    }
  }

  //-------------------------------------------------------------------
  def profile = deadbolt.Restrict(List(Array(Application.USER_ROLE_KEY)))() { implicit request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      val localUser = userService.getUser(context.session)
      Ok(views.html.profile(auth, userService, localUser.get))
    }
  }

  //-------------------------------------------------------------------
  def login() = Action { implicit request =>
    Ok(views.html.login(auth, userService, authProvider.getLoginForm))
  }

  //-------------------------------------------------------------------
  def doLogin = Action { implicit request =>
    val context = JavaHelpers.createJavaContext(request)
    val localUser = userService.getUser(context.session)
    val filledForm = authProvider.getLoginForm.bindFromRequest
    if (filledForm.hasErrors) {
      // User did not fill everything properly
      BadRequest(views.html.login(auth, userService, filledForm))
    }
    else {
      // Everything was filled
      JavaHelpers.createResult(context, authProvider.handleLogin(context))
    }
  }

  //-------------------------------------------------------------------
  def signup = Action { implicit request =>
    Ok(views.html.signup(auth, userService, authProvider.getSignupForm))
  }

  //-------------------------------------------------------------------
  def jsRoutes() = Action { implicit request =>
    Ok(play.routing.JavaScriptReverseRouter.create("jsRoutes",
      routes.javascript.Signup.forgotPassword)).as("text/javascript")
  }

  //-------------------------------------------------------------------
  def doSignup = Action { implicit request =>
    val context = JavaHelpers.createJavaContext(request)
    com.feth.play.module.pa.controllers.AuthenticateBase.noCache(context.response())
    val filledForm = authProvider.getSignupForm.bindFromRequest
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

/**
  * Application companion object containing key constants
  */
object Application {
  //-------------------------------------------------------------------
  val FLASH_MESSAGE_KEY = "message"
  val FLASH_ERROR_KEY = "error"
  val USER_ROLE_KEY = "user"
}
