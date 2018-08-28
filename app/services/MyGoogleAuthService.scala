package services

import dao.{GauthRecoveryTokenDao, UserDeviceDao}
import javax.inject.Inject



class MyGoogleAuthService @Inject() (gauthRecoveryTokenDao: GauthRecoveryTokenDao, userDeviceDao: UserDeviceDao) extends GoogleAuthService {
  override def isKnownDevice(userId: Long, deviceType: String, fingerprint: String): Boolean = {
    userDeviceDao.filter(userDevice => userDevice.`type` === deviceType)
  }

  override def isValidGAuthCode(userId: Long, code: String): Boolean = {

  }

  override def tryAuthenticateWithRecoveryToken(userId: Long, recoveryToken: String): Boolean = {

  }

  override def regenerateKey(userId: Long): (SharedSecret, Seq[RecoveryToken]) = {

  }

  override def disable(userId: Long): Unit = {

  }
}
