package dao

import org.scalatest.FunSpec
import org.scalatestplus.play.OneAppPerSuite
import play.api.Application

abstract class AbstractDaoFunSpec extends FunSpec with OneAppPerSuite {
  //------------------------------------------------------------------------
  // protected
  //------------------------------------------------------------------------
  protected def daoContext(implicit app: Application) = {
    Application.instanceCache[DaoContext].apply(app)
  }
}
