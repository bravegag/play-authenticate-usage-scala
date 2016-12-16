package generated

import be.objectify.deadbolt.scala.models._
import dao.UserDao
import generated.Tables.{LinkedAccountRow, UserRow}

/**
  * User adapter trait and object that offer implicit conversion from generated
  * UserRow to be.objectify.deadbolt.scala.models.Subject and other inline
  * operations available
  */
trait UserRowAdapter extends Subject {
  //------------------------------------------------------------------------
  def providers : Seq[String]
}

object UserRowAdapter {
  //------------------------------------------------------------------------
  implicit def toSubject(user : UserRow)(implicit userDao: UserDao) : UserRowAdapter = new UserRowAdapter {
    import utils.DbExecutionUtils._

    //------------------------------------------------------------------------
    override def identifier: String = user.id.toString

    //------------------------------------------------------------------------
    override def roles : List[Role] = {
      val roles = userDao.getRoles(user)
      roles.toList
    }

    //------------------------------------------------------------------------
    override def permissions : List[Permission] = {
      val permissions = userDao.getPermissions(user)
      permissions.toList
    }

    //------------------------------------------------------------------------
    override def providers : Seq[String] = {
      val providers : Seq[LinkedAccountRow] = userDao.getLinkedAccounts(user)
      providers.map(_.providerKey)
    }
  }
}