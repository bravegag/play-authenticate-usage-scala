package dao

import dao.generic.GenericDaoImpl
import generated.Tables.{GauthRecoveryTokenRow, _}
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

import scala.concurrent.Future

class UserDeviceDao  @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoImpl[UserDevice, UserDeviceRow, Long] (dbConfigProvider, UserDevice) {

  def get(userId: Long, deviceType: String, fingerprint: String): Future[Option[UserDeviceRow]] = {
    db.run(UserDevice.filter(device => device.`type` === deviceType && device.userId === userId && device.fingerprint === fingerprint).result.headOption)
  }

  def exists(userId: Long, deviceType: String, fingerprint: String): Future[Boolean] = {
    get(userId, deviceType, fingerprint).map(_.isDefined)
  }

}
