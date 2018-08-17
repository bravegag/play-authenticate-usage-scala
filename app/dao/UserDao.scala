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
  def roles(inputUser: UserRow) : Future[Seq[Role]] = {
    val action = (for {
      role <- SecurityRole
      userRole <- UserSecurityRole if role.id === userRole.securityRoleId
      user <- User if user.id === inputUser.id && userRole.userId === user.id
    } yield role).result

    db.run(action)
  }

  //------------------------------------------------------------------------
  def permissions(inputUser: UserRow) : Future[Seq[Permission]] = {
    val action = (for {
      permission <- SecurityPermission
      userPermission <- UserSecurityPermission if permission.id === userPermission.securityPermissionId
      user <- User if user.id === inputUser.id && userPermission.userId === user.id
    } yield permission).result

    db.run(action)
  }

  //------------------------------------------------------------------------
  def linkedAccounts(inputUser: UserRow) : Future[Seq[LinkedAccountRow]] = {
    val action = (for {
      linkedAccount <- LinkedAccount
      user <- User if user.id === inputUser.id && user.id === linkedAccount.userId
    } yield linkedAccount).result

    db.run(action)
  }

  //------------------------------------------------------------------------
    def findActiveByProviderKeyAndEmail(providerKey: String, email: String): Future[Option[UserRow]] = {
      val action = sql"""
          SELECT t1.*
          FROM "#${User.baseTableRow.tableName}" t1
          WHERE t1.active=true
           AND t1.email=$email
           AND EXISTS (SELECT * FROM #${LinkedAccount.baseTableRow.tableName} t2
                       WHERE t2.user_id=t1.id
                         AND t2.provider_key=$providerKey)
        """.as[UserRow].headOption
      db.run(action)
    }

  //------------------------------------------------------------------------
  def findActiveByProviderKeyAndPassword(providerKey: String, providerUserId: String): Future[Option[UserRow]] = {
    val action = sql"""
          SELECT t1.*
          FROM "#${User.baseTableRow.tableName}" t1
          WHERE t1.active=true
            AND EXISTS (SELECT * FROM #${LinkedAccount.baseTableRow.tableName} t2
                        WHERE t2.user_id=t1.id
                          AND t2.provider_key=$providerKey
                          AND t2.provider_user_id=$providerUserId)
      """.as[UserRow].headOption
    db.run(action)
  }

  //------------------------------------------------------------------------
  def findByEmail(email: String): Future[Seq[UserRow]] = {
    filter(_.email === email)
  }

  //------------------------------------------------------------------------
  def merge(targetUser: UserRow, sourceUser: UserRow) : Future[Unit] = {
    // deactivate the sourceUser
    val updateAction = User.filter(_.id === sourceUser.id).update(sourceUser.copy(active = false))

    // selects all linkedAccount from sourceUser but outputs targetUser's userId
    val selectAction = (for {
      linkedAccount <- LinkedAccount
      user <- User if user.id === sourceUser.id && user.id === linkedAccount.userId
    } yield (targetUser.id, linkedAccount.providerUserId, linkedAccount.providerKey, linkedAccount.modified)).
      result.map(seq => seq.map(LinkedAccountRow.tupled))

    // define an insert DBIOAction to insert all the selected linked accounts from sourceUser to targetUser
    val insertAction = selectAction.flatMap(LinkedAccount ++= _)

    // combine both actions using >> and transactionally
    db.run((updateAction >> insertAction).transactionally).map(_ => ())
  }
}