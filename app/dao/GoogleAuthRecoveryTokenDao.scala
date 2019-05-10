package dao

import java.sql.Timestamp
import java.util.Date

import dao.generic.GenericDaoImpl
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import generated.Tables._
import profile.api._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * Google Authentication Recovery Token DAO implementation
  * @param dbConfigProvider
  */
class GoogleAuthRecoveryTokenDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoImpl[GoogleAuthRecoveryToken, GoogleAuthRecoveryTokenRow, Long] (dbConfigProvider, GoogleAuthRecoveryToken) {

  def findByToken(userId: Long, token: String): Future[Option[GoogleAuthRecoveryTokenRow]] = {
    db.run(GoogleAuthRecoveryToken.filter(t => t.userId === userId && t.token === token).result.headOption)
  }

  def markAsUsed(gauthRecoveryTokenRow: GoogleAuthRecoveryTokenRow): Future[Unit] = {
    db.run(GoogleAuthRecoveryToken.filter(t => t.userId === gauthRecoveryTokenRow.userId && t.token === gauthRecoveryTokenRow.token)
      .update(gauthRecoveryTokenRow.copy(used = Some(new Timestamp(new Date().getTime))))).map(_ => ())
  }

  def findByUser(userId: Long): Future[Seq[GoogleAuthRecoveryTokenRow]] = {
    db.run(GoogleAuthRecoveryToken.filter(t => t.userId === userId && !t.used.isDefined).result)
  }
}