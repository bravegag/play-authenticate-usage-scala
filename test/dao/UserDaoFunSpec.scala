package dao

import generated.Tables.UserRow
import org.scalatest.Matchers
import play.api.test.WithApplication
import utils.AwaitUtils

import scala.concurrent.ExecutionContext.Implicits.global
import utils.AwaitUtils._

class UserDaoFunSpec extends DaoFunSpec with Matchers {
  describe("Create new user") {
    new WithApplication(app) {
      val dao = userDao
      AwaitUtils.await(dao.deleteAll)

      val result = (for {
        user <- dao.createAndFetch(UserRow(id = 0L, username = "test", email = "test@test.test", modified = None))
        all <- dao.findAll
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
}
