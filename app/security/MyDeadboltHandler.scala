package security

import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler, ExecutionContextProvider}
import com.feth.play.module.pa.PlayAuthenticate
import play.api.mvc._
import play.core.j.JavaHelpers
import services.UserService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MyDeadboltHandler()(implicit auth: PlayAuthenticate, context: ExecutionContextProvider, userService: UserService) extends DeadboltHandler {
	import services.PluggableUserService._

	//------------------------------------------------------------------------
	// public
	//------------------------------------------------------------------------
	override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future {
		val context = JavaHelpers.createJavaContext(request)
		if (auth.isLoggedIn(context.session())) {
			// user is logged in
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

	//------------------------------------------------------------------------
	override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = Future {
		val context = JavaHelpers.createJavaContext(request)
    val authUser = auth.getUser(context)
		// Caching might be a good idea here
		val user = userService.findByAuthUser(authUser)
		user
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