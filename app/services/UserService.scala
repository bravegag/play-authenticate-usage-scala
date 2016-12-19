package services

import java.sql.Timestamp

import com.feth.play.module.pa.PlayAuthenticate
import play.mvc.Http.Session
import javax.inject._
import java.util.Date

import controllers.Application
import be.objectify.deadbolt.scala.models.{Permission, Role}
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser
import com.feth.play.module.pa.service.AbstractUserService
import com.feth.play.module.pa.user._
import dao._
import generated.Tables.{LinkedAccountRow, UserRow}
import models.User

@Singleton
class UserService @Inject()(auth : PlayAuthenticate,
                            userDao: UserDao,
                            linkedAccountDao: LinkedAccountDao,
                            tokenActionDao: TokenActionDao,
                            securityRoleDao: SecurityRoleDao) extends AbstractUserService(auth) {
  import utils.DbExecutionUtils._

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def create(authUser: AuthUser) : UserRow = {
    val lastLogin = new Timestamp(new Date().getTime)
    val active = true
    val emailValidated = false
    val emptyUser = UserRow(0L, None, None, None, None, None, "N/A", "N/A", None,
      None, Option(lastLogin), active, emailValidated, None)
    val userToCreate = authUser match {
      case identity : EmailIdentity => {
        emptyUser.copy(email = identity.getEmail, emailValidated = false)
      }
      case identity : FirstLastNameIdentity => {
        emptyUser.copy(username = Option(identity.getName).getOrElse("N/A"),
          firstName = Option(identity.getFirstName), lastName = Option(identity.getLastName))
      }
      case identity : NameIdentity => {
        emptyUser.copy(username = Option(identity.getName).getOrElse("N/A"))
      }
    }

    // initialize security role
    val securityRole = securityRoleDao.findByName(Application.USER_ROLE_KEY).get

    // initialize linked account
    val linkedAccount = LinkedAccountRow(0L, authUser.getId, authUser.getProvider, None)

    val newUser = userDao.create(userToCreate, securityRole, linkedAccount)

    newUser
  }

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
  def linkedAccounts(user: UserRow) : Seq[LinkedAccountRow] = {
    userDao.linkedAccounts(user)
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
    val updated = user.copy(emailValidated = true)
    userDao.update(updated)
    tokenActionDao.deleteByUser(user, TokenAction.EMAIL_VERIFICATION)
  }

  //------------------------------------------------------------------------
  def findInSession(session: Session): Option[UserRow] = {
    val currentAuthUser = Option(auth.getUser(session))
    currentAuthUser match {
      case Some(authUser: UsernamePasswordAuthUser) => findByAuthUser(authUser)
      case Some(userIdentity: AuthUserIdentity) => findByAuthUser(userIdentity)
      case _ => None
    }
  }

  //------------------------------------------------------------------------
  def findByAuthUser(authUser: UsernamePasswordAuthUser): Option[UserRow] = {
    Option(authUser) match {
      case Some(authUser) => userDao.findActiveByProviderKeyAndEmail(authUser.getProvider, authUser.getEmail)
      case _ => None
    }
  }

  //------------------------------------------------------------------------
  def findByAuthUser(authUser: AuthUserIdentity): Option[UserRow] = {
    Option(authUser) match {
      case Some(authUser) => userDao.findActiveByProviderKeyAndUsername(authUser.getProvider, authUser.getId)
      case _ => None
    }
  }

  //------------------------------------------------------------------------
  def findByEmail(email: String): Option[UserRow] = {
    userDao.findByEmail(email).headOption
  }

  //------------------------------------------------------------------------
  override def getLocalIdentity(identity: AuthUserIdentity): AnyRef = {
    // For production: Caching might be a good idea here...
    // ...and don't forget to sync the cache when users get deactivated/deleted
    val option = findByAuthUser(identity)
    option match {
      case Some(user) => user.id
      case _ => null
    }
  }

  //------------------------------------------------------------------------
  override def merge(newAuthUser: AuthUser, oldAuthUser: AuthUser): AuthUser = {
    if (!oldAuthUser.equals(newAuthUser)) {
      val oldUser = findByAuthUser(oldAuthUser)
      val newUser = findByAuthUser(newAuthUser)

      // TODO: implement the merge
      newAuthUser

    } else {
      oldAuthUser
    }
  }

  //------------------------------------------------------------------------
  override def link(oldUser: AuthUser, newUser: AuthUser): AuthUser = {
    ???
  }

  //------------------------------------------------------------------------
  override def save(authUser: AuthUser): AnyRef = {
    val option = findByAuthUser(authUser)
    option.map { user : UserRow =>
      userDao.update(user.copy(lastLogin = Some(new Timestamp(new Date().getTime)))
    }
    authUser
  }
}