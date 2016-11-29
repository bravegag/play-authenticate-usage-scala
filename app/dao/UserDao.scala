package dao

import javax.inject.Singleton
import com.feth.play.module.pa.user.AuthUser
import generated.Tables._
import profile.api._

@Singleton
class UserDao extends GenericDao[User, UserRow, Long] (User) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def findByAuthUserIdentity(authUser: Option[AuthUser]): Option[User] = {
    // TODO: implement
    None
  }
}