package dao

import javax.inject._
import dao.generic.GenericDaoImpl

import scala.concurrent.Future
import generated.Tables._

import play.api.db.slick.DatabaseConfigProvider
import profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CookieTokenSeriesDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoImpl[CookieTokenSeries, CookieTokenSeriesRow, Long] (dbConfigProvider, CookieTokenSeries) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def create(user: UserRow, series: String, token: String) : Future[Unit] = {
    val newCookieTokenSeriesRow = CookieTokenSeriesRow(user.id, series, token, None) // Some(Timestamp.from(Instant.now))
    create(newCookieTokenSeriesRow)
  }

  //------------------------------------------------------------------------
  def findBySeries(userId: Long, series: String): Future[Option[CookieTokenSeriesRow]] = {
    db.run(CookieTokenSeries.filter(cookieTokenSeries => cookieTokenSeries.userId === userId &&
      cookieTokenSeries.series === series).result.headOption)
  }

  //------------------------------------------------------------------------
  def deleteBySeries(userId: Long, series: String): Future[Unit] = {
    db.run(CookieTokenSeries.filter(cookieTokenSeries => cookieTokenSeries.userId === userId &&
      cookieTokenSeries.series === series).delete).map(_ => ())
  }

  //------------------------------------------------------------------------
  def updateToken(cookieTokenSeries: CookieTokenSeriesRow, token: String): Future[Int] = {
    db.run(CookieTokenSeries.update(cookieTokenSeries.copy(token = token)))
  }
}