package services

import be.objectify.deadbolt.scala.models._
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser
import generated.Tables.{LinkedAccountRow, UserRow}

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

  //------------------------------------------------------------------------
  def resetPassword(authUser: UsernamePasswordAuthUser, create: Boolean): Unit

  //------------------------------------------------------------------------
  def verify: Unit

  //------------------------------------------------------------------------
  def linkedAccounts : Seq[LinkedAccountRow]
}

object PluggableUserService {
  /**
    * Enables converting from a UserRow to a PluggableUserService type and most importantly
    * to a Subject.
    * @param user the input user instance to convert to PluggableUserService
    * @param userService the implicit userService instance needed for doing the actual conversion
    */
  implicit class toPluggableUserService(user: UserRow)(implicit userService: UserService) extends PluggableUserService {
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

    //------------------------------------------------------------------------
    override def providers: Seq[String] = {
      userService.providers(user)
    }

    //------------------------------------------------------------------------
    override def changePassword(authUser: UsernamePasswordAuthUser, create: Boolean): Unit = {
      userService.changePassword(user, authUser, create)
    }

    //------------------------------------------------------------------------
    override def resetPassword(authUser: UsernamePasswordAuthUser, create: Boolean): Unit = {
      userService.resetPassword(user, authUser, create)
    }

    //------------------------------------------------------------------------
    def verify: Unit = {
      userService.verify(user)
    }

    //------------------------------------------------------------------------
    override def linkedAccounts: Seq[LinkedAccountRow] = {
      userService.linkedAccounts(user)
    }
  }

  /**
    * Enables converting from Option[UseRow] to Option[PluggableUserService] or
    * more importantly Option[Subject].
    * @param t user instance
    * @param ev evidence that can generate a PluggableUserService from a UserRow
    * @tparam T UserRow type
    * @return
    */
  implicit def toPluggableUserServiceOpt[T](t: Option[T])(implicit ev: T => PluggableUserService): Option[PluggableUserService] = t.map(ev)
}