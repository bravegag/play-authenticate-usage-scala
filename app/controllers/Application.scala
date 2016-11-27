package controllers

import javax.inject._

import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc._
import services.UserProvider

@Singleton
class Application @Inject() (auth: PlayAuthenticate, userProvider: UserProvider) extends Controller {
  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index() = Action {
    Ok(views.html.index.render(userProvider))
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