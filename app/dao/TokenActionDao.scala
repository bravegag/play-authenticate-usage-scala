package dao

import javax.inject._

import constants.TokenActionKey
import dao.generic._

import scala.concurrent.Future
import generated.Tables._
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TokenActionDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoImpl[TokenAction, TokenActionRow, Long] (dbConfigProvider, TokenAction) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def findByToken(token: String, `type`: TokenActionKey.Type): Future[Seq[TokenActionRow]] = {
    filter(tokenAction => tokenAction.token === token && tokenAction.`type` === `type`.toString)
  }

  //------------------------------------------------------------------------
  def deleteByUser(user: UserRow, `type`: TokenActionKey.Type) : Future[Unit] = {
    db.run(TokenAction.filter(tokenAction => tokenAction.userId === user.id &&
      tokenAction.`type` === `type`.toString).delete.map(_ => ()))
  }
}
