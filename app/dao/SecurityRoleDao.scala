package dao

import javax.inject._

import constants.SecurityRoleKey
import dao.generic.GenericDaoAutoIncImpl

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
  def findByName(securityRole: SecurityRoleKey.Type) : Future[Option[SecurityRoleRow]] = {
    db.run(SecurityRole.filter(_.name === securityRole.toString).result.headOption)
  }
}