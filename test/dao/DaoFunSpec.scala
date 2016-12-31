package dao

import org.scalatest.{BeforeAndAfter, FunSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.Application
import play.api.db.DBApi

abstract class DaoFunSpec extends FunSpec with OneAppPerSuite with BeforeAndAfter {
  lazy implicit val db = app.injector.instanceOf[DBApi].database("default")

  def daoContext(implicit app: Application) = {
    Application.instanceCache[DaoContext].apply(app)
  }
}
