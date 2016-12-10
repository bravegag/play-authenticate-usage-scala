package services

import com.feth.play.module.pa.PlayAuthenticate
import play.mvc.Http.Session
import javax.inject.Inject

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser
import com.feth.play.module.pa.user.AuthUserIdentity
import dao.UserDao
import generated.Tables._

class UserService @Inject()(auth : PlayAuthenticate, userDao: UserDao) {
  import dao.ExecHelper._

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def getUser(session: Session) : Option[UserRow] = {
    val currentAuthUser = Option(auth.getUser(session))
    currentAuthUser match {
      case None => None
      case Some(identity: UsernamePasswordAuthUser) => userDao.findActiveByProviderKeyAndEmail(identity.getProvider, identity.getEmail)
      case Some(identity: AuthUserIdentity) => userDao.findActiveByProviderKeyAndUsername(identity.getProvider, identity.getId)
    }
  }
}