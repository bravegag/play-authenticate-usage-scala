package dao

import constants.SecurityRoleKey
import generated.Tables.{LinkedAccountRow, SecurityRoleRow, UserRow}
import org.scalatest.Matchers
import play.api.test.WithApplication
import utils.AwaitUtils
import be.objectify.deadbolt.scala.models.Role

import scala.concurrent.ExecutionContext.Implicits.global
import utils.AwaitUtils._

class UserDaoFunSpec extends DaoFunSpec with Matchers {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  describe("Simple creation of a new user") {
    new WithApplication() {
      val dao = daoContext
      // ensure repeatability of the test
      AwaitUtils.await(dao.userDao.deleteAll)

      val result = (for {
        user <- dao.userDao.createAndFetch(UserRow(id = 0L, username = "test", email = "test@test.test", modified = None))
        all <- dao.userDao.findAll
      } yield (user, all))

      val user = result._1
      val all = result._2

      it("auto-generated UserRow#id should be valid") {
        user.id should equal (1L)
      }

      it("there must be only one user") {
        all.size should equal(1)
      }
    }
  }

  //------------------------------------------------------------------------
  describe("Create user with linked account and security role") {
    new WithApplication() {
      val dao = daoContext
      // ensure repeatability of the test
      AwaitUtils.await(dao.userDao.deleteAll)

      // initialize the security role as we know it exists
      val securityRole = SecurityRoleRow(id = 1L, name = SecurityRoleKey.USER_ROLE.toString)

      // declaring the type is critical here because the await function is implicitly
      // triggered only once and not every time we access a UserRow attribute
      val result = (for {
        user <- dao.userDao.create(UserRow(id = 0L, username = "test", email = "test@test.test", modified = None), securityRole, LinkedAccountRow(0L, "password", "xxx", None))
        linkedAccount <- dao.userDao.linkedAccounts(user)
        securityRoles <- dao.userDao.roles(user)
      } yield (user, linkedAccount, securityRoles))

      val user: UserRow = result._1
      val linkedAccount: Seq[LinkedAccountRow] = result._2
      val securityRoles: Seq[Role] = result._3

      it("user data must be correct") {
        user.id should equal (1L)
        user.username should equal ("test")
        user.email should equal ("test@test.test")
        user.modified should be (None)
      }

      it("user linked account must be correct") {
        linkedAccount.size should equal (1)
        linkedAccount.head.userId should equal (user.id)
        linkedAccount.head.providerKey should equal ("password")
        linkedAccount.head.providerPassword should equal ("xxx")
      }

      it("user security roles must be correct") {
        securityRoles.size should equal (1)
        securityRoles.head.name should equal (SecurityRoleKey.USER_ROLE.toString)
      }
    }
  }
}
