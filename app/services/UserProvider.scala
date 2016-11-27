package services

import javax.annotation.Nullable

import com.feth.play.module.pa.PlayAuthenticate
import com.feth.play.module.pa.user.AuthUser
import generated.Tables._
import play.mvc.Http.Session
import javax.inject.Inject

import dao.UserDao

class UserProvider @Inject() (auth : PlayAuthenticate, userDao: UserDao) {
  @Nullable
  def getUser(session: Session) : Option[User] = {
    val currentAuthUser = Option(auth.getUser(session))
    userDao.findByAuthUserIdentity(currentAuthUser)
  }
}