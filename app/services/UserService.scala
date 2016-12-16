package services

import com.feth.play.module.pa.PlayAuthenticate
import play.mvc.Http.Session
import javax.inject._

import be.objectify.deadbolt.scala.models.{Permission, Role}
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser
import com.feth.play.module.pa.user.AuthUserIdentity
import dao.UserDao
import generated.Tables._

@Singleton
class UserService @Inject()(auth : PlayAuthenticate, userDao: UserDao) {
  import utils.DbExecutionUtils._

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def identifier(user: UserRow): String = {
    user.id.toString
  }

  //------------------------------------------------------------------------
  def roles(user: UserRow) : List[Role] = {
    val roles = userDao.getRoles(user)
    roles.toList
  }

  //------------------------------------------------------------------------
  def permissions(user: UserRow) : List[Permission] = {
    val permissions = userDao.getPermissions(user)
    permissions.toList
  }

  //------------------------------------------------------------------------
  def providers(user: UserRow) : Seq[String] = {
    val providers : Seq[LinkedAccountRow] = userDao.getLinkedAccounts(user)
    providers.map(_.providerKey)
  }

  //------------------------------------------------------------------------
  def changePassword(user: UserRow, authUser: UsernamePasswordAuthUser, create: Boolean): Unit = {
    // TODO: implement
  }

  //------------------------------------------------------------------------
  def getUser(session: Session): Option[UserRow] = {
    val currentAuthUser = Option(auth.getUser(session))
    currentAuthUser match {
      case None => None
      case Some(identity: UsernamePasswordAuthUser) => userDao.findActiveByProviderKeyAndEmail(identity.getProvider, identity.getEmail)
      case Some(identity: AuthUserIdentity) => userDao.findActiveByProviderKeyAndUsername(identity.getProvider, identity.getId)
    }
  }

  //------------------------------------------------------------------------
  def findByEmail(email: String): Option[UserRow] = {
    userDao.findByEmail(email).headOption
  }
}