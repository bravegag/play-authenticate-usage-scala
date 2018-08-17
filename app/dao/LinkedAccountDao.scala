package dao

import javax.inject._

import dao.generic._

import scala.concurrent.Future
import generated.Tables._
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class LinkedAccountDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoImpl[LinkedAccount, LinkedAccountRow, Long] (dbConfigProvider, LinkedAccount) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def create(user: UserRow, providerUserId: String, providerKey: String) : Future[Unit] = {
    val newLinkedAccount = LinkedAccountRow(user.id, providerUserId, providerKey, None, None)
    create(newLinkedAccount)
  }

  //------------------------------------------------------------------------
  def findByProviderKey(user: UserRow, providerKey: String): Future[Seq[LinkedAccountRow]] = {
    filter(linkedAccount => linkedAccount.userId === user.id &&
      linkedAccount.providerKey === providerKey)
  }

  //------------------------------------------------------------------------
  def findBySeries(user: UserRow, series: String): Future[Option[LinkedAccountRow]] = {
    db.run(LinkedAccount.filter(linkedAccount => linkedAccount.userId === user.id &&
      linkedAccount.series === series).result.headOption)
  }

  //------------------------------------------------------------------------
  def deleteByKeyAndProviderUserId(key: String, providerUserId: String): Future[Unit] = {
    db.run(LinkedAccount.filter(linkedAccount => linkedAccount.providerKey === key &&
      linkedAccount.providerUserId === providerUserId).delete).map(_ => ())
  }
}