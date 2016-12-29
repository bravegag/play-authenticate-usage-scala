package dao

import generated.Tables.UserRow
import play.api.test.WithApplication
import scala.concurrent.ExecutionContext.Implicits.global
import utils.AwaitUtils._

class UserDaoFunSpec extends DaoFunSpec {
  describe("Testing") {
    new WithApplication() {
      val dao = userDao

      val result = (for {
        user <- dao.createAndFetch(UserRow(id = 0L, username = "test", email = "test@test.test", modified = None))
        all <- dao.findAll
      } yield (user, all))

      val user = result._1
      val all = result._2

      it("auto generated UserRow#id should be valid") {
        assert(user.id > 0L)
        println("auto generated UserRow#id is: %d".format(user.id))
      }

      it("the evolutions didn't reset") {
        assert(all.size == 1)
      }
    }
  }
}
