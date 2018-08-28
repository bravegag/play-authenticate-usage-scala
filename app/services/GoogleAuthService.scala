package services

trait GoogleAuthService {
  def isKnownDevice(userId: Long, deviceType: String, fingerprint: String): Boolean

  def isValidGAuthCode(userId: Long, code: String): Boolean

  def tryAuthenticateWithRecoveryToken(userId: Long, recoveryToken: String): Boolean

  type RecoveryToken = String
  type SharedSecret = String
  def regenerateKey(userId: Long): (SharedSecret, Seq[RecoveryToken])

  def disable(userId: Long): Unit
}
