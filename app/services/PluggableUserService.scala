package services

import be.objectify.deadbolt.scala.models._
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser
import generated.Tables.UserRow

/**
  * UserRow adapter trait and object that offer implicit conversion from generated
  * UserRow to be.objectify.deadbolt.scala.models.Subject and other User sensitive
  * inline operations. The implementations are provided by the UserService
  */
trait PluggableUserService extends Subject {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def providers : Seq[String]

  //------------------------------------------------------------------------
  def changePassword(authUser: UsernamePasswordAuthUser, create: Boolean): Unit
}

object PluggableUserService {
  implicit class toPluggable(user: UserRow)(implicit userService: UserService) extends PluggableUserService {
    //------------------------------------------------------------------------
    override def providers: Seq[String] = {
      userService.providers(user)
    }

    //------------------------------------------------------------------------
    override def changePassword(authUser: UsernamePasswordAuthUser, create: Boolean): Unit = {
      userService.changePassword(user, authUser, create)
    }

    //------------------------------------------------------------------------
    override def identifier: String = {
      userService.identifier(user)
    }

    //------------------------------------------------------------------------
    override def roles: List[Role] = {
      userService.roles(user)
    }

    //------------------------------------------------------------------------
    override def permissions: List[Permission] = {
      userService.permissions(user)
    }
  }
}