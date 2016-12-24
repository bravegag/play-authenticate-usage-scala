package dao

import javax.inject.{Inject, Singleton}

import dao.generic.GenericDaoAutoIncImpl

import scala.concurrent.Future
import generated.Tables.{ SecurityRole => SecurityRoleTQ, _ }
import controllers.SecurityRole
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

@Singleton
class SecurityRoleDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoAutoIncImpl[SecurityRoleTQ, SecurityRoleRow, Long] (dbConfigProvider, SecurityRoleTQ) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def findByName(securityRole: SecurityRole.Type) : Future[Option[SecurityRoleRow]] = {
    db.run(SecurityRoleTQ.filter(_.name === securityRole.toString).result.headOption)
  }
}