package dao

import javax.inject.{Inject, Singleton}

import dao.generic.GenericDaoAutoIncImpl
import generated.Tables.{SecurityRole, SecurityRoleRow}

import scala.concurrent.Future
import generated.Tables._
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

@Singleton
class SecurityRoleDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoAutoIncImpl[SecurityRole, SecurityRoleRow, Long] (dbConfigProvider, SecurityRole) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def findByName(name: String) : Future[Option[SecurityRoleRow]] = {
    db.run(SecurityRole.filter(_.name === name).result.headOption)
  }
}