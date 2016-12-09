package controllers

import javax.inject._

import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import dao.UserDao
import play.api.mvc._
import play.Configuration
import play.mvc.Http
import services.UserProvider
import scala.collection.JavaConversions

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Application @Inject() (implicit configuration: Configuration, deadbolt: DeadboltActions,
      auth: PlayAuthenticate, userProvider: UserProvider, userDao: UserDao) extends Controller {
  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index() = Action {
    Ok(views.html.index(userProvider))
  }

  //-------------------------------------------------------------------
  def restricted = deadbolt.Restrict(List(Array(USER_ROLE)))() { request =>
    Future {
      val session : Http.Session = new Http.Session(JavaConversions.mapAsJavaMap(request.session.data))
      val user = userProvider.getUser(session)
      Ok(views.html.restricted(userProvider, user))
    }
  }

  //-------------------------------------------------------------------
  def profile = deadbolt.Restrict(List(Array(USER_ROLE)))() { request =>
    Future {
      val session : Http.Session = new Http.Session(JavaConversions.mapAsJavaMap(request.session.data))
      val user = userProvider.getUser(session)
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

  //-------------------------------------------------------------------
  // members
  //-------------------------------------------------------------------
  val FLASH_MESSAGE_KEY = "message"
  val FLASH_ERROR_KEY = "error"
  val USER_ROLE = "user"
}