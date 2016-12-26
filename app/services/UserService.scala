package services

import be.objectify.deadbolt.scala.models.{Permission, Role}
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser
import com.feth.play.module.pa.user.{AuthUser, AuthUserIdentity}
import play.mvc.Http.Session
import generated.Tables._

/**
  * Service interface definition for a User. This should be the only
  * point of contact and dependency for doing User operations in the
  * rest of the application outside the dao and services packages.
  */
trait UserService {
  //------------------------------------------------------------------------
  def create(authUser: AuthUser): UserRow

  //------------------------------------------------------------------------
  def identifier(user: UserRow): String

  //------------------------------------------------------------------------
  def roles(user: UserRow): List[Role]

  //------------------------------------------------------------------------
  def permissions(user: UserRow): List[Permission]

  //------------------------------------------------------------------------
  def providers(user: UserRow): Seq[String]

  //------------------------------------------------------------------------
  def linkedAccounts(user: UserRow): Seq[LinkedAccountRow]

  //------------------------------------------------------------------------
  def changePassword(user: UserRow, authUser: UsernamePasswordAuthUser, create: Boolean): Unit

  //------------------------------------------------------------------------
  def resetPassword(user: UserRow, authUser: UsernamePasswordAuthUser, create: Boolean): Unit

  //------------------------------------------------------------------------
  def verify(user: UserRow): Unit

  //------------------------------------------------------------------------
  def findInSession(session: Session): Option[UserRow]

  //------------------------------------------------------------------------
  def findByAuthUser(identity: AuthUserIdentity): Option[UserRow]

  //------------------------------------------------------------------------
  def findByEmail(email: String): Option[UserRow]

  //------------------------------------------------------------------------
  def save(authUser: AuthUser): AnyRef

  //------------------------------------------------------------------------
  def getLocalIdentity(identity: AuthUserIdentity): AnyRef

  //------------------------------------------------------------------------
  def merge(newAuthUser: AuthUser, oldAuthUser: AuthUser): AuthUser

  //------------------------------------------------------------------------
  def link(oldAuthUser: AuthUser, newAuthUser: AuthUser): AuthUser

  //------------------------------------------------------------------------
  def update(authUser: AuthUser): AuthUser
}
