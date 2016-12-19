package security

import be.objectify.deadbolt.scala.cache.HandlerCache
import play.api.Configuration
import play.api.Environment
import play.api.inject.Binding
import play.api.inject.Module

import scala.collection.Seq
import javax.inject.Singleton

@Singleton
class MyCustomDeadboltHook extends Module {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(bind[HandlerCache].to[MyHandlerCache].in[Singleton])
  }
}