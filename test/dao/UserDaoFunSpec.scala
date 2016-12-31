package dao

import constants.SecurityRoleKey
import generated.Tables.{LinkedAccountRow, SecurityRoleRow, UserRow}
import org.scalatest.Matchers
import play.api.test.WithApplication
import utils.AwaitUtils
import be.objectify.deadbolt.scala.models.{Role, Permission}

import scala.concurrent.ExecutionContext.Implicits.global
import utils.AwaitUtils._

class UserDaoFunSpec extends DaoFunSpec with Matchers {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  describe("Create user") {
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

      val result = (for {
        user <- dao.userDao.create(UserRow(id = 0L, username = "test", email = "test@test.test",
          active = true, modified = None), securityRole, LinkedAccountRow(0L, "password", "xxx", None))
        linkedAccount <- dao.userDao.linkedAccounts(user)
        securityRoles <- dao.userDao.roles(user)
        permissions <- dao.userDao.permissions(user)
      } yield (user, linkedAccount, securityRoles, permissions))

      val user: UserRow = result._1
      val linkedAccount: Seq[LinkedAccountRow] = result._2
      val securityRoles: Seq[Role] = result._3
      val permissions: Seq[Permission] = result._4

      it("user data should be correct") {
        user.id should equal (1L)
        user.username should equal ("test")
        user.email should equal ("test@test.test")
        user.active should be (true)
        user.modified should be (None)
      }

      it("user linked account should be correct") {
        linkedAccount.size should equal (1)
        linkedAccount.head.userId should equal (user.id)
        linkedAccount.head.providerKey should equal ("password")
        linkedAccount.head.providerPassword should equal ("xxx")
      }

      it("user security roles should be correct") {
        securityRoles.size should equal (1)
        securityRoles.head.name should equal (SecurityRoleKey.USER_ROLE.toString)
      }

      it("user permissions should be empty") {
        permissions.isEmpty should be (true)
      }
    }

    //------------------------------------------------------------------------
    describe("Find active user by provider key and email") {
      new WithApplication() {
        val dao = daoContext

        // reuses the user created in the previous test
        val user: Option[UserRow] = (for {
          user <- dao.userDao.findActiveByProviderKeyAndEmail("password", "test@test.test")
        } yield user)

        it("the user was found") {
          user should not be (None)
        }
      }
    }

    //------------------------------------------------------------------------
    describe("Find active user by provider key and password") {
      new WithApplication() {
        val dao = daoContext

        // reuses the user created in the previous test
        val user: Option[UserRow] = (for {
          user <- dao.userDao.findActiveByProviderKeyAndPassword("password", "xxx")
        } yield user)

        it("the user was found") {
          user should not be (None)
        }
      }
    }
  }
}
