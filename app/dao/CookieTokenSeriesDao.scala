package dao

import javax.inject._
import dao.generic.GenericDaoImpl

import scala.concurrent.Future
import generated.Tables._
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

@Singleton
class CookieTokenSeriesDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoImpl[CookieTokenSeries, CookieTokenSeriesRow, Long] (dbConfigProvider, CookieTokenSeries) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def create(user: UserRow, series: String, token: String) : Future[Unit] = {
    val newCookieTokenSeries = CookieTokenSeriesRow(user.id, series, token, None)
    create(newCookieTokenSeries)
  }

  //------------------------------------------------------------------------
  def findBySeries(user: UserRow, series: String): Future[Option[CookieTokenSeriesRow]] = {
    db.run(CookieTokenSeries.filter(cookieTokenSeries => cookieTokenSeries.userId === user.id &&
      cookieTokenSeries.series === series).result.headOption)
  }

  //------------------------------------------------------------------------
  def updateToken(cookieTokenSeries: CookieTokenSeriesRow, token: String): Future[Int] = {
    db.run(CookieTokenSeries.update(cookieTokenSeries.copy(token = token)))
  }
}