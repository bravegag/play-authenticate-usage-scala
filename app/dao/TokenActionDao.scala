package dao

import javax.inject._

import controllers.TokenAction
import dao.generic._

import scala.concurrent.Future
import generated.Tables.{TokenAction => TokenActionTQ, _}
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TokenActionDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericDaoImpl[TokenActionTQ, TokenActionRow, Long] (dbConfigProvider, TokenActionTQ) {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def findByToken(token: String, `type`: TokenAction.Type): Future[Seq[TokenActionRow]] = {
    filter(tokenAction => tokenAction.token === token && tokenAction.`type` === `type`.toString)
  }

  //------------------------------------------------------------------------
  def deleteByUser(user: UserRow, `type`: TokenAction.Type) : Future[Unit] = {
    // TODO: review this implementation
    db.run(TokenActionTQ.filter(tokenAction => tokenAction.userId === user.id && tokenAction.`type` === `type`.toString).delete.map( _ => ()))
  }
}
