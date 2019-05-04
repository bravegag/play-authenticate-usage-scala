package services

trait GoogleAuthService {
  protected val PROVIDER_KEY : String = "gauth"

  def getProviderKey: String = PROVIDER_KEY

  def isKnownDevice(userEmail: String, deviceType: String, fingerprint: String): Boolean

  def isValidGAuthCode(userEmail: String, code: Int): Boolean

  def tryAuthenticateWithRecoveryToken(userEmail: String, recoveryToken: String): Boolean

  type RecoveryToken = String
  type SharedSecret = String
  def regenerateKey(userId: Long): (SharedSecret, Seq[RecoveryToken])

  def getUserSharedKey(userId: Long): SharedSecret

  def getUserRecoveryTokens(userId: Long): Seq[RecoveryToken]

  def disable(userId: Long): Unit

  def isEnabled(userId: Long): Boolean

  def isEnabled(email: String): Boolean
}
