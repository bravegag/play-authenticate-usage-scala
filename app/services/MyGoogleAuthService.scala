package services

import java.sql.Timestamp
import java.util.Date
import java.util.UUID

import com.warrenstrange.googleauth.GoogleAuthenticator
import dao.{GauthRecoveryTokenDao, LinkedAccountDao, UserDao, UserDeviceDao}
import generated.Tables.{GauthRecoveryTokenRow, LinkedAccountRow}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MyGoogleAuthService @Inject() (
                                      gauthRecoveryTokenDao: GauthRecoveryTokenDao,
                                      userDeviceDao: UserDeviceDao,
                                      linkedAccountDao: LinkedAccountDao,
                                      userDao: UserDao)
  extends GoogleAuthService {

  private val googleAuthenticator = new GoogleAuthenticator()

  override def isKnownDevice(userEmail: String, deviceType: String, fingerprint: String): Boolean = {
    getUserIdByEmail(userEmail).fold(false) { userId =>
      await(userDeviceDao.exists(userId, deviceType, fingerprint))
    }
  }

  override def isValidGAuthCode(userId: Long, code: Int): Boolean = {

    val sharedSecretOpt : Option[String] = getSharedSecret(userId).map(_.providerUserId)

    sharedSecretOpt.fold(false) { secret =>
      googleAuthenticator.authorize(secret, code)
    }
  }

  override def tryAuthenticateWithRecoveryToken(userId: Long, recoveryToken: String): Boolean = {
    await(gauthRecoveryTokenDao.findByToken(userId, recoveryToken)).fold(false) { token =>
      await(gauthRecoveryTokenDao.markAsUsed(token).map(_ => true))
    }
  }

  override def regenerateKey(userId: Long): (SharedSecret, Seq[RecoveryToken]) = {
    disable(userId)

    val newSharedKey = googleAuthenticator.createCredentials()

    val user = await(userDao.findById(userId))

    await(
      for {
        _ <- linkedAccountDao.create(user.get, newSharedKey.getKey, PROVIDER_KEY)
      } yield ()
    )

    val tokens : Seq[String] =
      for(_ <- 1 to 8) yield {
        val token = UUID.randomUUID().toString
        await(gauthRecoveryTokenDao.create(GauthRecoveryTokenRow(id = -1, userId, token, new Timestamp(new Date().getTime))))
        token
      }

    (newSharedKey.getKey, tokens)
  }

  override def disable(userId: Long): Unit = {
    getSharedSecret(userId).map(_.id).map(linkedAccountDao.delete).foreach(await)
  }

  override def isEnabled(userId: Long): Boolean = {
    getSharedSecret(userId).isDefined
  }

  private def getSharedSecret(userId: Long): Option[LinkedAccountRow] = {
    await(
      for {
        user <- userDao.findById(userId)
        linkedAccount <- linkedAccountDao.findByProviderKey(user.get, PROVIDER_KEY)
      } yield linkedAccount.headOption
    )
  }

  private def await[T](f : Future[T]): T = {
    Await.result(f, 10 seconds)
  }

  override def isEnabled(email: String): Boolean = {
    getUserIdByEmail(email).fold(false) { userId =>
      isEnabled(userId)
    }
  }

  private def getUserIdByEmail(email: String): Option[Long] = {
    await(userDao.findByEmail(email)).headOption.map(_.id)
  }

  override def getUserSharedKey(userId: Long): SharedSecret = {
    getSharedSecret(userId).map(_.providerUserId).get
  }

  override def getUserRecoveryTokens(userId: Long): Seq[RecoveryToken] = {
    await(gauthRecoveryTokenDao.findByUser(userId)).map(_.token)
  }
}
