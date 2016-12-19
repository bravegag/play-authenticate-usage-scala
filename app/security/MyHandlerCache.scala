package security;

import be.objectify.deadbolt.scala.{DeadboltHandler, ExecutionContextProvider, HandlerKey}
import be.objectify.deadbolt.scala.cache.HandlerCache
import javax.inject.Inject
import javax.inject.Singleton

import com.feth.play.module.pa.PlayAuthenticate
import services.UserService;

@Singleton
class MyHandlerCache @Inject() (auth: PlayAuthenticate,
																context: ExecutionContextProvider,
																userService: UserService) extends HandlerCache {
	//------------------------------------------------------------------------
	// public
	//------------------------------------------------------------------------
	override def apply(key: HandlerKey): DeadboltHandler = myDeadboltHandler

	//------------------------------------------------------------------------
	override def apply(): DeadboltHandler = myDeadboltHandler

	//------------------------------------------------------------------------
	// private
	//------------------------------------------------------------------------
	private lazy val myDeadboltHandler = new MyDeadboltHandler()(auth, context, userService)
}