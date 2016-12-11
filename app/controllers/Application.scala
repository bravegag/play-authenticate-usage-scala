package controllers

import javax.inject._

import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc._
import services.UserService
import dao.UserDao
import play.api.i18n.{I18nSupport, MessagesApi}
import play.core.j.JavaHelpers

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Application @Inject() (implicit
                             val messagesApi: MessagesApi,
                             deadbolt: DeadboltActions,
                             auth: PlayAuthenticate,
                             userService: UserService,
                             userDao: UserDao) extends Controller with I18nSupport {

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index = Action {
    Ok(views.html.index(userService))
  }

  //-------------------------------------------------------------------
  def restricted = deadbolt.Restrict(List(Array(Application.USER_ROLE_KEY)))() { request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      val localUser = userService.getUser(context.session)
      Ok(views.html.restricted(userService, localUser))
    }
  }

  //-------------------------------------------------------------------
  def profile = deadbolt.Restrict(List(Array(Application.USER_ROLE_KEY)))() { request =>
    Future {
      val context = JavaHelpers.createJavaContext(request)
      val localUser = userService.getUser(context.session)
      Ok(views.html.profile(auth, userService, localUser.get))
    }
  }

/*
  TODO: migrate
  //-------------------------------------------------------------------
  def login() = Action {
    Ok(views.html.login(auth, userProvider, provider.getLoginForm))
  }
*/
  //-------------------------------------------------------------------
  def jsRoutes() = Action {
  	// TODO: migrate
/*
    Ok(play.routing.JavaScriptReverseRouter.create("jsRoutes",
      routes.javascript.Signup.forgotPassword)).as("text/javascript")
*/
	  Ok("TODO: migrate")
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
