package dao

import generated.Tables.UserRow
import org.scalatest.{BeforeAndAfterAll, FunSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.test.WithApplication
import play.api.Application
import scala.concurrent.ExecutionContext.Implicits.global
import utils.AwaitUtils._

class UserDaoFunSpec extends FunSpec with OneAppPerSuite with BeforeAndAfterAll {
  lazy implicit val db = app.injector.instanceOf[DBApi].database("test")

  override def beforeAll() {
    Evolutions.applyEvolutions(db)
  }

  override def afterAll() {
    Evolutions.cleanupEvolutions(db)
  }

  def userDao(implicit app: Application) = {
    Application.instanceCache[UserDao].apply(app)
  }

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
