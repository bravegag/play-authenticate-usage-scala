package dao

import javax.inject._

import be.objectify.deadbolt.scala.models._
import dao.generic.GenericDaoAutoIncImpl

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import generated.Tables._
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

@Singleton
class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
    extends GenericDaoAutoIncImpl[User, UserRow, Long] (dbConfigProvider, User) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def create(inputUser: UserRow, inputSecurityRole: SecurityRoleRow,
             inputLinkedAccount: LinkedAccountRow) : Future[UserRow] = {
    val insertion = (for {
      user <- (User returning User.map(_.id) into ((row, id) => row.copy(id = id)) += inputUser)
      linkedAccount <- (LinkedAccount += inputLinkedAccount.copy(userId = user.id))
      userSecurityRole <- (UserSecurityRole += UserSecurityRoleRow(user.id, inputSecurityRole.id, None))
    } yield user).transactionally
    db.run(insertion)
  }

  //------------------------------------------------------------------------
  def roles(user: UserRow) : Future[Seq[Role]] = {
    val action = (for {
      role <- SecurityRole
      userRole <- UserSecurityRole if role.id === userRole.securityRoleId
      user <- User if userRole.userId === user.id
    } yield role).result

    db.run(action)
  }

  //------------------------------------------------------------------------
  def permissions(user: UserRow) : Future[Seq[Permission]] = {
    val action = (for {
      permission <- SecurityPermission
      userPermission <- UserSecurityPermission if permission.id === userPermission.securityPermissionId
      user <- User if userPermission.userId === user.id
    } yield permission).result

    db.run(action)
  }

  //------------------------------------------------------------------------
  def linkedAccounts(user: UserRow) : Future[Seq[LinkedAccountRow]] = {
    val action = (for {
      linkedAccount <- LinkedAccount
      user <- User if linkedAccount.userId === user.id
    } yield linkedAccount).result

    db.run(action)
  }

  //------------------------------------------------------------------------
  def findActiveByProviderKeyAndEmail(providerKey: String, email: String): Future[Option[UserRow]] = {
    val action = sql"""SELECT t1.* FROM \"user\" t1 " +
                       "WHERE t1.active=true AND " +
                       "      t1.email=${email} AND " +
                       "      EXISTS (SELECT * FROM linked_account t2 " +
                       "              WHERE t2.user_id = t1.id AND " +
                       "                    t2.provider_key = ${providerKey})""".as[UserRow].headOption
    db.run(action)
  }

  //------------------------------------------------------------------------
  def findActiveByProviderKeyAndUsername(providerKey: String, providerUserName: String): Future[Option[UserRow]] = {
    val action = sql"""SELECT t1.* FROM \"user\" t1 " +
                       "WHERE t1.active=true AND "
                             "EXISTS (SELECT * FROM linked_account t2 " +
                                     "WHERE t2.user_id = t1.id AND " +
                                           "t2.provider_key = ${providerKey} AND " +
                                           "t2.provider_username = ${providerUserName})""".as[UserRow].headOption
    db.run(action)
  }

  //------------------------------------------------------------------------
  def findByEmail(email: String): Future[Seq[UserRow]] = {
    filter(_.email === email)
  }
}