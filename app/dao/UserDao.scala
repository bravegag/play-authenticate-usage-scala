package dao

import javax.inject.{Inject, Singleton}

import com.feth.play.module.pa.user.AuthUser
import generated.Tables._
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

@Singleton
class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends GenericDaoImpl[User, UserRow, Long] (dbConfigProvider, User) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def findByAuthUserIdentity(authUser: Option[AuthUser]): Option[User] = {
    // TODO: implement
    None
  }
}