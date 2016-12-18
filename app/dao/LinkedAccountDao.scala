package dao

import javax.inject._

import com.feth.play.module.pa.user.AuthUser
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
  def create(user: UserRow, authUser: AuthUser) : LinkedAccountRow = {
    // TODO: investigate why here it was passing authUser.getId as password
    val newLinkedAccount = LinkedAccountRow(user.id, authUser.getId, authUser.getProvider, None)
    create(newLinkedAccount)
    newLinkedAccount
  }

  //------------------------------------------------------------------------
  def findByProviderKey(user: UserRow, providerKey: String): Future[Seq[LinkedAccountRow]] = {
    filter(linkedAccount => linkedAccount.userId === user.id &&
      linkedAccount.providerKey === providerKey)
  }
}