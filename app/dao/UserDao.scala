package dao

import javax.inject.Inject

import com.feth.play.module.pa.user.AuthUser
import generated.Tables.{User, UserRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile

import scala.concurrent.Future

class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  def count(): Future[Int] = db.run(User.length.result)

  def findById(id: Long): Future[Option[UserRow]] = db.run(User.filter(_.id === id).result.headOption)

  def findAll(): Future[Seq[UserRow]] = db.run(User.result)

  def create(user: UserRow): Future[UserRow] = {
    val insertQuery = User returning User.map(_.id) into ((user, id) => user.copy(id = id))
    val action = insertQuery += user
    db.run(action)
  }

  def create(users: Seq[UserRow]): Future[Unit] = db.run(User ++= users).map(_ => ())

  def update(update: UserRow): Future[Unit] = {
    db.run(User.filter(_.id === update.id).update(update)).map(_ => ())
  }

  def delete(id: Long): Future[Unit] = db.run(User.filter(_.id === id).delete).map(_ => ())

  def findByAuthUserIdentity(authUser: Option[AuthUser]): Option[User] = {
    // TODO: implement
    None
  }
}