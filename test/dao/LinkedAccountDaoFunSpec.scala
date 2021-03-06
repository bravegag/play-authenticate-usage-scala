package dao

import generated.Tables.{LinkedAccountRow, UserRow}
import org.scalatest.Matchers
import play.api.test.WithApplication
import helpers.AwaitHelpers

import scala.concurrent.ExecutionContext.Implicits.global
import helpers.AwaitHelpers._

import scala.Long

class LinkedAccountDaoFunSpec extends AbstractDaoFunSpec with Matchers {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  describe("Create linked account ") {
    new WithApplication() {
      val dao = daoContext
      // ensure repeatability of the test
      AwaitHelpers.await(dao.userDao.deleteAll)

      val user = UserRow(id = 0L, username = "test", email = "test@test.test", modified = None)

      val result: Option[LinkedAccountRow] = (for {
        user <- dao.userDao.createAndFetch(user)
        _ <- dao.linkedAccountDao.create(user, "xxx", "xxx")
        linkedAccount <- dao.linkedAccountDao.findById(user.id)
      } yield linkedAccount)

      it("linked account must be correct") {
        result should not be None
        val linkedAccount = result.get
        linkedAccount.userId should equal(1L)
        linkedAccount.providerUserId should equal("xxx")
        linkedAccount.providerKey should equal("password")
      }
    }
  }

  //------------------------------------------------------------------------
  describe("find by provider key") {
    new WithApplication() {
      val dao = daoContext

      // reuse the previous user
      val user = UserRow(id = 1L, username = "test", email = "test@test.test", modified = None)

      val linkedAccounts: Seq[LinkedAccountRow] = (for {
        linkedAccount <- dao.linkedAccountDao.findByProviderKey(user, "password")
      } yield linkedAccount)

      it("the linked account was found") {
        linkedAccounts.size should be (1)
        val linkedAccount = linkedAccounts.head
        linkedAccount.userId should equal(1L)
        linkedAccount.providerUserId should equal("xxx")
        linkedAccount.providerKey should equal("password")
      }
    }
  }
}
