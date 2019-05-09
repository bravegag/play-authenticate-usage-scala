package security

import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler, ExecutionContextProvider}
import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc._
import services.{GoogleAuthService, UserService}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import support.JContextSupport.RequestToContext

class MyDeadboltHandler()(implicit auth: PlayAuthenticate, context: ExecutionContextProvider, userService: UserService, googleAuthService: GoogleAuthService) extends DeadboltHandler {
	import services.PluggableUserService._

	//------------------------------------------------------------------------
	// public
	//------------------------------------------------------------------------
	override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future {
    Option(auth).flatMap { auth =>
      request.jContextOption match {
        case Some(context) => {
          if (auth.isLoggedIn(context)) {
            None
          } else {
            // user is not logged in
            // call this if you want to redirect your visitor to the page that
            // was requested before sending him to the login page
            // if you don't call this, the user will get redirected to the page
            // defined by your resolver
            val originalUrl = auth.storeOriginalUrl(context)
            context.flash.put("error", "You need to log in first, to view '" + originalUrl + "'")
            // we know auth.getResolver.login is an instance of Scala's Call
            Some(Results.Redirect(auth.getResolver.login.asInstanceOf[play.api.mvc.Call]))
          }
        }
        case _ => None
      }
    }.orElse(None)
  }

	//------------------------------------------------------------------------
	override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = Future {
    Option(auth).flatMap { auth =>
      request.jContextOption match {
        case Some(context) => {
          val authUser = auth.getUser(context)
          // Caching might be a good idea here
          val user = userService.findByAuthUser(authUser)
          user
        }
        case _ => None
      }
    }.orElse(None)
	}

	//------------------------------------------------------------------------
	override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future {
		None
	}

	//------------------------------------------------------------------------
	override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = Future {
		Results.Forbidden("Forbidden")
	}
}