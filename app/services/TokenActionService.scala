package services

import java.util.Date
import javax.inject.{Inject, Singleton}

import com.feth.play.module.pa.PlayAuthenticate
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
  def findByToken(token: String, `type`: TokenAction.Type): Option[TokenActionRow] = {
    tokenActionDao.findByToken(token, `type`).headOption
  }

  //------------------------------------------------------------------------
  def isValid(tokenAction: TokenActionRow): Boolean = {
    tokenAction.expires.get.after(new Date())
  }

  //------------------------------------------------------------------------
  def targetUser(tokenAction: TokenActionRow): Option[UserRow] = {
    userDao.findById(tokenAction.userId.get)
  }
}
