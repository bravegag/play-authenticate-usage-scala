package services

import javax.annotation.Nullable
import com.feth.play.module.pa.PlayAuthenticate
import generated.Tables._
import play.mvc.Http.Session
import javax.inject.Inject
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser
import com.feth.play.module.pa.user.AuthUserIdentity
import dao.UserDao

class UserProvider @Inject() (auth : PlayAuthenticate, userDao: UserDao) {
  import dao.BlockUntilDoneHelper._

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  @Nullable
  def getUser(session: Session) : Option[UserRow] = {
    val currentAuthUser = Option(auth.getUser(session))
    currentAuthUser match {
      case None => None
      case Some(identity: UsernamePasswordAuthUser) => userDao.findActiveByProviderKeyAndEmail(identity.getProvider, identity.getEmail)
      case Some(identity: AuthUserIdentity) => userDao.findActiveByProviderKeyUserName(identity.getProvider, identity.getId)
    }
  }
}