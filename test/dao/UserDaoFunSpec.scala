package dao

import constants.SecurityRoleKey
import generated.Tables.{LinkedAccountRow, SecurityRoleRow, UserRow}
import org.scalatest.Matchers
import play.api.test.WithApplication
import utils.AwaitUtils
import be.objectify.deadbolt.scala.models.{Role, Permission}

import scala.concurrent.ExecutionContext.Implicits.global
import utils.AwaitUtils._

class UserDaoFunSpec extends AbstractDaoFunSpec with Matchers {
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

      val user: UserRow = result._1
      val all = result._2

      it("user should be correct") {
        user.id should equal (1L)
        user.username should equal ("test")
        user.email should equal ("test@test.test")
        user.modified should not be None
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
      val linkedAccounts: Seq[LinkedAccountRow] = result._2
      val securityRoles: Seq[Role] = result._3
      val permissions: Seq[Permission] = result._4

      it("user should be correct") {
        user.id should equal (1L)
        user.username should equal ("test")
        user.email should equal ("test@test.test")
        user.active should be (true)
      }

      it("user linked account should be correct") {
        linkedAccounts.size should equal (1)
        val linkedAccount = linkedAccounts.head
        linkedAccount.userId should equal (user.id)
        linkedAccount.providerKey should equal ("password")
        linkedAccount.providerPassword should equal ("xxx")
        linkedAccount.modified should not be None
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

    //------------------------------------------------------------------------
    describe("Find user by email") {
      new WithApplication() {
        val dao = daoContext

        // reuses the user created in the previous test
        val users: Seq[UserRow] = (for {
          user <- dao.userDao.findByEmail("test@test.test")
        } yield user)

        it("the user was found") {
          users.size should equal (1)
        }
      }
    }

    //------------------------------------------------------------------------
    describe("Merge two users") {
      new WithApplication() {
        val dao = daoContext

        // we know it ...
        val sourceUserId = 1L

        // reuses the user created in the previous test
        val result = (for {
          targetUser <- dao.userDao.createAndFetch(UserRow(id = 0L, username = "target", active = true, email = "target@target.target", modified = None))
          _ <- dao.userDao.merge(targetUser, dao.userDao.findById(sourceUserId).get)
          linkedAccounts <- dao.userDao.linkedAccounts(targetUser)
          sourceUser <- dao.userDao.findById(sourceUserId)
        } yield (targetUser, linkedAccounts, sourceUser))

        val targetUser: UserRow = result._1
        val linkedAccounts: Seq[LinkedAccountRow] = result._2
        val sourceUser: Option[UserRow] = result._3

        it("target user should be correct") {
          targetUser.id should equal (2L)
          targetUser.username should equal ("target")
          targetUser.email should equal ("target@target.target")
          targetUser.active should be (true)
          targetUser.modified should not be (None)
        }

        it("target user linked account should be correct") {
          linkedAccounts.size should equal (1)
          val linkedAccount = linkedAccounts.head
          linkedAccount.userId should equal (targetUser.id)
          linkedAccount.providerKey should equal ("password")
          linkedAccount.providerPassword should equal ("xxx")
          linkedAccount.modified should not be (None)
        }

        it("source user should be inactive") {
          sourceUser should not be None
          sourceUser.get.active should be (false)
        }
      }
    }
  }
}
