package services

import com.feth.play.module.pa.PlayAuthenticate
import play.mvc.Http.Session
import javax.inject._

import be.objectify.deadbolt.scala.models.{Permission, Role}
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser
import com.feth.play.module.pa.user.AuthUserIdentity
import dao._
import generated.Tables.{ UserRow, LinkedAccountRow }

@Singleton
class UserService @Inject()(auth : PlayAuthenticate,
                            userDao: UserDao,
                            linkedAccountDao: LinkedAccountDao,
                            tokenActionDao: TokenActionDao) {
  import utils.DbExecutionUtils._

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def identifier(user: UserRow): String = {
    user.id.toString
  }

  //------------------------------------------------------------------------
  def roles(user: UserRow) : List[Role] = {
    val roles = userDao.roles(user)
    roles.toList
  }

  //------------------------------------------------------------------------
  def permissions(user: UserRow) : List[Permission] = {
    val permissions = userDao.permissions(user)
    permissions.toList
  }

  //------------------------------------------------------------------------
  def providers(user: UserRow) : Seq[String] = {
    val providers : Seq[LinkedAccountRow] = userDao.linkedAccounts(user)
    providers.map(_.providerKey)
  }

  //------------------------------------------------------------------------
  def changePassword(user: UserRow, authUser: UsernamePasswordAuthUser, create: Boolean): Unit = {
    val option = linkedAccountDao.findByProviderKey(user, authUser.getProvider).headOption
    val linkedAccount = option match {
      case Some(linkedAccount) => linkedAccount
      case None => {
        if (create) {
          val newLinkedAccount = LinkedAccountRow(user.id, authUser.getProvider, authUser.getId, None)
          linkedAccountDao.create(newLinkedAccount)
          newLinkedAccount

        } else {
          throw new RuntimeException("Account not enabled for password usage")
        }
      }
    }
    val update = linkedAccount.copy(providerPassword = authUser.getHashedPassword)
    linkedAccountDao.update(update)
  }

  //------------------------------------------------------------------------
  def resetPassword(user: UserRow, authUser: UsernamePasswordAuthUser, create: Boolean): Unit = {
    changePassword(user, authUser, create)
    tokenActionDao.deleteByUser(user, TokenAction.PASSWORD_RESET)
  }

  //------------------------------------------------------------------------
  def verify(user: UserRow) : Unit = {
    val updated = user.copy(emailValidated = Some(true))
    userDao.update(updated)
    tokenActionDao.deleteByUser(user, TokenAction.EMAIL_VERIFICATION)
  }

  //------------------------------------------------------------------------
  def findInSession(session: Session): Option[UserRow] = {
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