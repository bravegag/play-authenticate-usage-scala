package services

import java.sql.Timestamp

import com.feth.play.module.pa.PlayAuthenticate
import play.mvc.Http.Session
import javax.inject._
import java.util.Date

import be.objectify.deadbolt.scala.models.{Permission, Role}
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser
import com.feth.play.module.pa.service.AbstractUserService
import com.feth.play.module.pa.user._
import constants.{SecurityRoleKey, TokenActionKey}
import dao._
import generated.Tables.{LinkedAccountRow, UserRow}

@Singleton
class UserServiceImpl @Inject()(auth : PlayAuthenticate,
                                daoContext: DaoContext) extends AbstractUserService(auth) with UserService {
  import utils.AwaitUtils._

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  override def create(authUser: AuthUser) : UserRow = {
    val lastLogin = new Timestamp(new Date().getTime)
    val active = true
    val emailValidated = false
    var newUser = UserRow(id = 0L, firstName = None, middleName = None, lastName = None,
      dateOfBirth = None, username = "N/A", email = "N/A", lastLogin = Option(lastLogin),
      active, emailValidated, modified = None)

    newUser = authUser match {
      case identity: EmailIdentity => {
        newUser.copy(email = identity.getEmail, emailValidated = false)
      }
      case _ => newUser
    }

    newUser = authUser match {
      case identity: FirstLastNameIdentity => {
        newUser.copy(username = Option(identity.getName).getOrElse("N/A"),
          firstName = Option(identity.getFirstName), lastName = Option(identity.getLastName))
      }
      case identity: NameIdentity => {
        newUser.copy(username = Option(identity.getName).getOrElse("N/A"))
      }
      case _ => newUser
    }

    // initialize security role
    val securityRole = daoContext.securityRoleDao.findByName(SecurityRoleKey.USER_ROLE).get

    // initialize linked account
    val linkedAccount = LinkedAccountRow(0L, authUser.getId, authUser.getProvider, None, None)

    daoContext.userDao.create(newUser, securityRole, linkedAccount)
  }

  //------------------------------------------------------------------------
  override def identifier(user: UserRow): String = {
    user.id.toString
  }

  //------------------------------------------------------------------------
  override def roles(user: UserRow) : List[Role] = {
    val roles = daoContext.userDao.roles(user)
    roles.toList
  }

  //------------------------------------------------------------------------
  override def permissions(user: UserRow) : List[Permission] = {
    val permissions = daoContext.userDao.permissions(user)
    permissions.toList
  }

  //------------------------------------------------------------------------
  override def providers(user: UserRow) : Seq[String] = {
    val providers : Seq[LinkedAccountRow] = daoContext.userDao.linkedAccounts(user)
    providers.map(_.providerKey)
  }

  //------------------------------------------------------------------------
  override def linkedAccounts(user: UserRow) : Seq[LinkedAccountRow] = {
    daoContext.userDao.linkedAccounts(user)
  }

  //------------------------------------------------------------------------
  override def changePassword(user: UserRow, authUser: UsernamePasswordAuthUser, create: Boolean): Unit = {
    val option = daoContext.linkedAccountDao.findByProviderKey(user, authUser.getProvider).headOption
    val linkedAccount = option match {
      case Some(linkedAccount) => linkedAccount
      case None => {
        if (create) {
          val newLinkedAccount = LinkedAccountRow(user.id, authUser.getId, authUser.getProvider, None, None)
          daoContext.linkedAccountDao.create(newLinkedAccount)
          newLinkedAccount

        } else {
          throw new RuntimeException("Account not enabled for password usage")
        }
      }
    }
    daoContext.linkedAccountDao.update(linkedAccount.copy(providerUserId = authUser.getHashedPassword))
  }

  //------------------------------------------------------------------------
  override def resetPassword(user: UserRow, authUser: UsernamePasswordAuthUser, create: Boolean): Unit = {
    changePassword(user, authUser, create)
    daoContext.tokenActionDao.deleteByUser(user, TokenActionKey.PASSWORD_RESET)
  }

  //------------------------------------------------------------------------
  override def verify(user: UserRow) : Unit = {
    val updated = user.copy(emailValidated = true)
    daoContext.userDao.update(updated)
    daoContext.tokenActionDao.deleteByUser(user, TokenActionKey.EMAIL_VERIFICATION)
  }

  //------------------------------------------------------------------------
  override def findInSession(session: Session): Option[UserRow] = {
    val option = Option(auth.getUser(session))
    option match {
      case Some(identity: AuthUserIdentity) => findByAuthUser(identity)
      case _ => None
    }
  }

  //------------------------------------------------------------------------
  override def findByAuthUser(identity: AuthUserIdentity): Option[UserRow] = {
    Option(identity) match {
      case Some(authUser: UsernamePasswordAuthUser) => daoContext.userDao.findActiveByProviderKeyAndEmail(authUser.getProvider, authUser.getEmail)
      case Some(authUser: AuthUserIdentity) => daoContext.userDao.findActiveByProviderKeyAndPassword(authUser.getProvider, authUser.getId)
      case _ => None
    }
  }

  //------------------------------------------------------------------------
  override def findByEmail(email: String): Option[UserRow] = {
    daoContext.userDao.findByEmail(email).headOption
  }

  //------------------------------------------------------------------------
  override def save(authUser: AuthUser): AnyRef = {
    val option = authUser match {
      case identity: AuthUserIdentity => findByAuthUser(identity)
      case _ => None
    }
    val id = option.map(_.id).getOrElse(null.asInstanceOf[Long])
    Long.box(id)
  }

  //------------------------------------------------------------------------
  override def getLocalIdentity(identity: AuthUserIdentity): AnyRef = {
    // For production: Caching might be a good idea here...
    // ...and don't forget to sync the cache when users get deactivated/deleted
    val option = findByAuthUser(identity)
    val result : AnyRef = option match {
      case Some(user) => Long.box(user.id)
      case _ => null
    }
    result
  }

  //------------------------------------------------------------------------
  override def merge(newAuthUser: AuthUser, oldAuthUser: AuthUser): AuthUser = {
    if (!oldAuthUser.equals(newAuthUser)) {
      val oldUserOpt = findByAuthUser(oldAuthUser)
      val newUserOpt = findByAuthUser(newAuthUser)

      (oldUserOpt, newUserOpt) match {
        case (Some(oldUser), Some(newUser)) => {
          // new user is no longer active
          daoContext.userDao.merge(oldUser, newUser)
        }
        case _ => // TODO: the most sensible thing to do is to throw an exception
      }
    }
    oldAuthUser
  }

  //------------------------------------------------------------------------
  override def link(oldAuthUser: AuthUser, newAuthUser: AuthUser): AuthUser = {
    if (!oldAuthUser.equals(newAuthUser)) {
      val oldUserOpt = findByAuthUser(oldAuthUser)
      val newUserOpt = findByAuthUser(newAuthUser)

      (oldUserOpt, newUserOpt) match {
        case (Some(oldUser: UserRow), Some(_)) => {
          // link the two users
          daoContext.linkedAccountDao.create(oldUser, providerUserId = newAuthUser.getId,
            providerKey = newAuthUser.getProvider)
        }
        case _ => // TODO: the most sensible thing to do is to throw an exception
      }
    }
    newAuthUser
  }

  //------------------------------------------------------------------------
  override def unlink(knownUser: AuthUser): Unit = {
    // deletes the linked account by provider and provider user id
    daoContext.linkedAccountDao.deleteByKeyAndProviderUserId(knownUser.getProvider, knownUser.getId)
  }

  //------------------------------------------------------------------------
  override def update(authUser: AuthUser): AuthUser = {
    val option = findByAuthUser(authUser)
    option.map { user : UserRow =>
      daoContext.userDao.update(user.copy(lastLogin = Some(new Timestamp(new Date().getTime))))
    }
    authUser
  }
}