package dao

import javax.inject._

import dao.generic._

import scala.concurrent.Future
import generated.Tables._
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

@Singleton
class LinkedAccountDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoImpl[LinkedAccount, LinkedAccountRow, Long] (dbConfigProvider, LinkedAccount) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def create(user: UserRow, providerKey: String, providerPassword: String) : Future[Unit] = {
    val newLinkedAccount = LinkedAccountRow(user.id, providerKey, providerPassword, None)
    create(newLinkedAccount)
  }

  //------------------------------------------------------------------------
  def findByProviderKey(user: UserRow, providerKey: String): Future[Seq[LinkedAccountRow]] = {
    filter(linkedAccount => linkedAccount.userId === user.id &&
      linkedAccount.providerKey === providerKey)
  }
}