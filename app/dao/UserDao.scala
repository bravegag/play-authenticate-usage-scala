package dao

import javax.inject.{Inject, Singleton}

import scala.concurrent.Future
import generated.Tables._
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

@Singleton
class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends GenericDaoAutoIncImpl[User, UserRow, Long] (dbConfigProvider, User) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def findActiveByProviderKeyAndEmail(providerKey: String, email: String): Future[Option[UserRow]] = {
    val action = sql"""SELECT t1.* FROM \"user\" t1 " +
                       "WHERE t1.active=true AND " +
                       "      t1.email=${email} AND " +
                       "      EXISTS (SELECT * FROM linked_account t2 " +
                       "              WHERE t2.user_id = t1.id AND " +
                       "                    t2.provider_key = ${providerKey})""".as[UserRow].headOption
    db.run(action)
  }

  //------------------------------------------------------------------------
  def findActiveByProviderKeyUserName(providerKey: String, providerUserName: String): Future[Option[UserRow]] = {
    val action = sql"""SELECT t1.* FROM \"user\" t1 " +
                       "WHERE t1.active=true AND "
                             "EXISTS (SELECT * FROM linked_account t2 " +
                                     "WHERE t2.user_id = t1.id AND " +
                                           "t2.provider_key = ${providerKey} AND " +
                                           "t2.provider_username = ${providerUserName})""".as[UserRow].headOption
    db.run(action)
  }
}