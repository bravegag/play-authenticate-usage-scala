package dao

import javax.inject._

import dao.generic._

import scala.concurrent.Future
import generated.Tables.{ TokenAction => TokenActionTQ, _}
import play.api.db.slick.DatabaseConfigProvider
import profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

object TokenAction extends Enumeration {
  type Type = Value
  val EMAIL_VERIFICATION = Value("EV")
  val PASSWORD_RESET = Value("PR")
}

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
