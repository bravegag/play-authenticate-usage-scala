package services

trait GoogleAuthService {

  protected val PROVIDER_KEY : String = "gauth"

  def isKnownDevice(userId: Long, deviceType: String, fingerprint: String): Boolean

  def isValidGAuthCode(userId: Long, code: Int): Boolean

  def tryAuthenticateWithRecoveryToken(userId: Long, recoveryToken: String): Boolean

  type RecoveryToken = String
  type SharedSecret = String
  def regenerateKey(userId: Long): (SharedSecret, Seq[RecoveryToken])

  def disable(userId: Long): Unit

  def isEnabled(userId: Long): Boolean
}
