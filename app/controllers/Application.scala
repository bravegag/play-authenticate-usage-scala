package controllers

import javax.inject._

import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc._
import services.UserService
import dao.UserDao
import play.api.i18n.{I18nSupport, MessagesApi}

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Application @Inject() (implicit
                             val messagesApi: MessagesApi,
                             deadbolt: DeadboltActions,
                             auth: PlayAuthenticate,
                             userService: UserService,
                             userDao: UserDao) extends Controller with I18nSupport {
  import utils.PlayConversions._

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index = Action {
    Ok(views.html.index(userService))
  }

  //-------------------------------------------------------------------
  def restricted = deadbolt.Restrict(List(Array(ApplicationKeys.UserRole)))() { request =>
    Future {
      val localUser = userService.getUser(request.session)
      Ok(views.html.restricted(userService, localUser))
    }
  }

  //-------------------------------------------------------------------
  def profile = deadbolt.Restrict(List(Array(ApplicationKeys.UserRole)))() { request =>
    Future {
      val localUser = userService.getUser(request.session)
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