package services

import java.sql.Timestamp
import java.util.Date
import javax.inject.{Inject, Singleton}

import com.feth.play.module.pa.PlayAuthenticate
import constants.TokenActionKey
import generated.Tables.{TokenActionRow, UserRow}
import dao._

@Singleton
class TokenActionServiceImpl @Inject()(auth : PlayAuthenticate,
                                       daoContext: DaoContext) extends TokenActionService {
  import utils.AwaitUtils._

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  override def create(user: UserRow, `type`: TokenActionKey.Type, token: String) : TokenActionRow = {
    val created = new Timestamp(new Date().getTime)
    val expires = new Timestamp(created.getTime + VERIFICATION_TIME * 1000)
    val tokenAction = TokenActionRow(user.id, token, `type`.toString,
      created, expires, None)
    daoContext.tokenActionDao.create(tokenAction)
    tokenAction
  }

  //------------------------------------------------------------------------
  override def findByToken(token: String, `type`: TokenActionKey.Type): Option[TokenActionRow] = {
    daoContext.tokenActionDao.findByToken(token, `type`).headOption
  }

  //------------------------------------------------------------------------
  override def isValid(tokenAction: TokenActionRow): Boolean = {
    tokenAction.expires.after(new Date())
  }

  //------------------------------------------------------------------------
  override def targetUser(tokenAction: TokenActionRow): Option[UserRow] = {
    daoContext.userDao.findById(tokenAction.userId)
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
