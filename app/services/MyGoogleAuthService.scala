package services

import java.sql.Timestamp
import java.util.Date
import java.util.UUID

import com.warrenstrange.googleauth.GoogleAuthenticator
import dao._
import generated.Tables.{GoogleAuthRecoveryTokenRow, LinkedAccountRow}
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MyGoogleAuthService @Inject() (userDeviceDao: UserDeviceDao,
                                     linkedAccountDao: LinkedAccountDao,
                                     daoContext: DaoContext) extends GoogleAuthService {
  import helpers.AwaitHelpers._

  private val googleAuthenticator = new GoogleAuthenticator()

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  override def isKnownDevice(userEmail: String, deviceType: String, fingerprint: String): Boolean = {
    getUserIdByEmail(userEmail).fold(false) { userId =>
      userDeviceDao.exists(userId, deviceType, fingerprint)
    }
  }

  //------------------------------------------------------------------------
  override def isValidGAuthCode(userEmail: String, code: Int): Boolean = {
    getUserIdByEmail(userEmail).fold(false) { userId => {
        val sharedSecretOpt : Option[String] = getSharedSecret(userId).map(_.providerUserId)
        sharedSecretOpt.fold(false) { secret =>
          googleAuthenticator.authorize(secret, code)
        }
      }
    }
  }

  //------------------------------------------------------------------------
  override def tryAuthenticateWithRecoveryToken(userEmail: String, recoveryToken: String): Boolean = {
    getUserIdByEmail(userEmail).fold(false) { userId =>
      daoContext.gauthRecoveryTokenDao.findByToken(userId, recoveryToken).fold(false) { token =>
        if(token.used.isEmpty) {
          daoContext.gauthRecoveryTokenDao.markAsUsed(token).map(_ => true)
        } else {
          false
        }
      }
    }
  }

  //------------------------------------------------------------------------
  override def regenerateKey(userId: Long): (SharedSecret, Seq[RecoveryToken]) = {
    disable(userId)

    val newSharedKey = googleAuthenticator.createCredentials()

    val user = daoContext.userDao.findById(userId)

    linkedAccountDao.create(user.get, newSharedKey.getKey, PROVIDER_KEY)

    val tokens : Seq[String] =
      for(_ <- 1 to 8) yield {
        val token = UUID.randomUUID().toString
        daoContext.gauthRecoveryTokenDao.create(GoogleAuthRecoveryTokenRow(userId, token, new Timestamp(new Date().getTime)))
        token
      }

    (newSharedKey.getKey, tokens)
  }

  //------------------------------------------------------------------------
  override def disable(userId: Long): Unit = {
    getSharedSecret(userId).map(_.providerUserId).foreach(key => await(linkedAccountDao.deleteByProvider(PROVIDER_KEY, key)))
    await(daoContext.gauthRecoveryTokenDao.delete(userId))
  }

  //------------------------------------------------------------------------
  override def isEnabled(userId: Long): Boolean = {
    getSharedSecret(userId).isDefined
  }

  //------------------------------------------------------------------------
  override def isEnabled(email: String): Boolean = {
    getUserIdByEmail(email).fold(false) { userId =>
      isEnabled(userId)
    }
  }

  //------------------------------------------------------------------------
  override def getUserSharedKey(userId: Long): SharedSecret = {
    getSharedSecret(userId).map(_.providerUserId).get
  }

  //------------------------------------------------------------------------
  override def getUserRecoveryTokens(userId: Long): Seq[RecoveryToken] = {
    val recoveryTokens: Seq[GoogleAuthRecoveryTokenRow] = daoContext.gauthRecoveryTokenDao.findByUser(userId)
    recoveryTokens.map(_.token)
  }

  //------------------------------------------------------------------------
  // private
  //------------------------------------------------------------------------
  private def getSharedSecret(userId: Long): Option[LinkedAccountRow] = {
    for {
      user <- daoContext.userDao.findById(userId)
      linkedAccount <- linkedAccountDao.findByProviderKey(user.get, PROVIDER_KEY)
    } yield linkedAccount.headOption
  }

  //------------------------------------------------------------------------
  private def getUserIdByEmail(email: String): Option[Long] = {
    daoContext.userDao.findByEmail(email).headOption.map(_.id)
  }
}
