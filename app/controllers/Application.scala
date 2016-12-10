package controllers

import javax.inject._
import be.objectify.deadbolt.scala.DeadboltActions
import com.feth.play.module.pa.PlayAuthenticate
import dao._
import play.api.mvc._
import services.UserProvider
import play.api.i18n._
import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class Application @Inject() (implicit val messagesApi: MessagesApi, deadbolt: DeadboltActions, auth: PlayAuthenticate,
                             userProvider: UserProvider, userDao: UserDao) extends Controller with I18nSupport {
  import utils.PlayConversions._

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  def index = Action {
    Ok(views.html.index(userProvider))
  }

  //-------------------------------------------------------------------
  def restricted = deadbolt.Restrict(List(Array(ApplicationKeys.UserRole)))() { request =>
    Future {
      val localUser = userProvider.getUser(request.session)
      Ok(views.html.restricted(userProvider, localUser))
    }
  }

  //-------------------------------------------------------------------
  def profile = deadbolt.Restrict(List(Array(ApplicationKeys.UserRole)))() { request =>
    Future {
      val localUser = userProvider.getUser(request.session)
      Ok(views.html.profile(auth, userProvider, localUser.get))
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