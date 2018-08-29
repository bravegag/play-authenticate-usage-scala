package dao

import java.sql.Timestamp
import java.util.Date

import dao.generic.GenericDaoImpl
import generated.Tables.{CookieTokenSeries, GauthRecoveryTokenRow}
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import generated.Tables._
import profile.api._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class GauthRecoveryTokenDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoImpl[GauthRecoveryToken, GauthRecoveryTokenRow, Long] (dbConfigProvider, GauthRecoveryToken) {

  def findByToken(userId: Long, token: String): Future[Option[GauthRecoveryTokenRow]] = {
    db.run(GauthRecoveryToken.filter(t => t.userId === userId && t.token === token).result.headOption)
  }

  def markAsUsed(gauthRecoveryTokenRow: GauthRecoveryTokenRow): Future[Unit] = {
    db.run(GauthRecoveryToken.update(gauthRecoveryTokenRow.copy(used = Some(new Timestamp(new Date().getTime))))).map(_ => ())
  }

  def findByUser(userId: Long): Future[Seq[GauthRecoveryTokenRow]] = {
    db.run(GauthRecoveryToken.filter(t => t.userId === userId && t.used.isDefined).result)
  }
}
