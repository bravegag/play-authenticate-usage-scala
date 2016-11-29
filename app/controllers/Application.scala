package controllers

import javax.inject._

import com.feth.play.module.pa.PlayAuthenticate
import dao.UserDao
import generated.Tables.UserRow
import play.api.mvc._
import services.UserProvider
import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Application @Inject() (auth: PlayAuthenticate, userProvider: UserProvider, userDao: UserDao) extends Controller {
  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index() = Action {
    // create a sample user
    val user = new UserRow(id = 0, username = Some("bravegag"), firstName = Some("Giovanni"),
      lastName = Some("Azua"), modified = None)
    userDao.create(user).onSuccess { case x => println(x) }

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