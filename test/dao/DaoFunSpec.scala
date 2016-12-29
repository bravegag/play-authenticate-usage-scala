package dao

import org.scalatest.{BeforeAndAfterAll, FunSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.Application
import play.api.db.evolutions.Evolutions
import play.api.db.DBApi

abstract class DaoFunSpec extends FunSpec with OneAppPerSuite with BeforeAndAfterAll {
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
}
