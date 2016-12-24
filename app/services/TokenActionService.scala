package services

import java.sql.Timestamp
import java.util.Date
import javax.inject.{Inject, Singleton}

import com.feth.play.module.pa.PlayAuthenticate
import controllers.TokenAction
import dao._
import generated.Tables.{TokenActionRow, UserRow}

@Singleton
class TokenActionService @Inject()(auth : PlayAuthenticate,
                                   tokenActionDao: TokenActionDao,
                                   userDao: UserDao) {
  import utils.DbExecutionUtils._

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def create(user: UserRow, `type`: TokenAction.Type, token: String) : TokenActionRow = {
    val created = new Timestamp(new Date().getTime)
    val expires = new Timestamp(created.getTime + VERIFICATION_TIME * 1000)
    val tokenAction = TokenActionRow(user.id, token, `type`.toString,
      created, expires, None)
    tokenActionDao.create(tokenAction)
    tokenAction
  }

  //------------------------------------------------------------------------
  def findByToken(token: String, `type`: TokenAction.Type): Option[TokenActionRow] = {
    tokenActionDao.findByToken(token, `type`).headOption
  }

  //------------------------------------------------------------------------
  def isValid(tokenAction: TokenActionRow): Boolean = {
    tokenAction.expires.after(new Date())
  }

  //------------------------------------------------------------------------
  def targetUser(tokenAction: TokenActionRow): Option[UserRow] = {
    userDao.findById(tokenAction.userId)
  }

  //------------------------------------------------------------------------
  // private
  //------------------------------------------------------------------------
  /**
    * Verification time frame (until the user clicks on the link in the email)
    * in seconds. Defaults to one week
    */
  private val VERIFICATION_TIME = 7 * 24 * 3600
}
