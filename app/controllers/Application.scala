package controllers

import javax.inject._

import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import dao.UserDao
import play.api.mvc._
import play.Configuration
import services.UserProvider

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Application @Inject() (implicit configuration: Configuration, deadbolt: DeadboltActions,
      auth: PlayAuthenticate, userProvider: UserProvider, userDao: UserDao) extends Controller {
  import utils.PlayConversions._

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index() = Action {
    Ok(views.html.index(userProvider))
  }

  //-------------------------------------------------------------------
  def restricted = deadbolt.Restrict(List(Array(ApplicationKeys.UserRole)))() { request =>
    Future {
      val user = userProvider.getUser(request.session)
      Ok(views.html.restricted(userProvider, user))
    }
  }

  //-------------------------------------------------------------------
  def profile = deadbolt.Restrict(List(Array(ApplicationKeys.UserRole)))() { request =>
    Future {
      val user = userProvider.getUser(request.session)
      Ok(views.html.profile(auth, userProvider, user.get))
    }
  }

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