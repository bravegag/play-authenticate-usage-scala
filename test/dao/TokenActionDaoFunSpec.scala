package dao

import java.sql.Timestamp
import java.util.{Date, UUID}

import constants.TokenActionKey
import generated.Tables.{TokenActionRow, UserRow}
import org.scalatest.Matchers
import play.api.test.WithApplication
import utils.AwaitUtils
import utils.AwaitUtils._

import scala.concurrent.ExecutionContext.Implicits.global

class TokenActionDaoFunSpec extends AbstractDaoFunSpec with Matchers {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  describe("Create token action") {
    new WithApplication() {
      val dao = daoContext

      // ensure repeatability of the test
      AwaitUtils.await(dao.userDao.deleteAll)

      val user = UserRow(id = 0L, username = "test", email = "test@test.test", modified = None)
      val uuid = UUID.randomUUID.toString
      val created = new Timestamp(new Date().getTime)
      val expires = new Timestamp(created.getTime + 1000)
      val tokenAction = TokenActionRow(0L, uuid, TokenActionKey.EMAIL_VERIFICATION.toString,
        created, expires, None)


      val result: Option[TokenActionRow] = (for {
        user <- dao.userDao.createAndFetch(user)
        _ <- dao.tokenActionDao.create(tokenAction.copy(userId = user.id))
        tokenAction <- dao.tokenActionDao.findById(user.id)
      } yield tokenAction)

      it("linked account must be correct") {
        result should not be None
        val tokenAction = result.get
        tokenAction.userId should equal(1L)
        tokenAction.token should equal(uuid)
        tokenAction.`type` should equal (TokenActionKey.EMAIL_VERIFICATION.toString)
      }
    }
  }

  //------------------------------------------------------------------------
  describe("Create token with invalid type") {
    new WithApplication() {
      val dao = daoContext

      val uuid = UUID.randomUUID.toString
      val created = new Timestamp(new Date().getTime)
      val expires = new Timestamp(created.getTime + 1000)
      val tokenAction = TokenActionRow(1L, uuid, "UN", created, expires, None)

      val caught = intercept[org.postgresql.util.PSQLException] {
        val result: Option[TokenActionRow] = (for {
          _ <- dao.tokenActionDao.create(tokenAction)
          tokenAction <- dao.tokenActionDao.findById(1L)
        } yield tokenAction)
      }

      it("PSQLException exception is received") {
        assert(caught.getMessage.contains("ERROR: new row for relation \"token_action\" violates check constraint \"token_action_type_check\""))
      }
    }
  }

  //------------------------------------------------------------------------
  describe("Delete by user") {
    new WithApplication() {
      val dao = daoContext

      // reuse the user previously created
      val user = UserRow(id = 1L, username = "test", email = "test@test.test", modified = None)

      // let's delete the token action previously created
      // but first check that it is there just to be sure
      val result = (for {
        tokenAction <- dao.tokenActionDao.findById(1L)
        _ <- dao.tokenActionDao.deleteByUser(user, TokenActionKey.EMAIL_VERIFICATION)
        all <- dao.tokenActionDao.findAll
      } yield (tokenAction, all))

      val tokenAction : Option[TokenActionRow] = result._1
      val all : Seq[TokenActionRow] = result._2

      it("token action should be deleted") {
        tokenAction should not be None
        all.isEmpty should be (true)
      }
    }
  }
}
