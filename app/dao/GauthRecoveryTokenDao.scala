package dao

import dao.generic.GenericDaoImpl
import generated.Tables.{CookieTokenSeries, GauthRecoveryTokenRow}
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import generated.Tables._

class GauthRecoveryTokenDao  @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoImpl[GauthRecoveryToken, GauthRecoveryTokenRow, Long] (dbConfigProvider, GauthRecoveryToken) {


}
