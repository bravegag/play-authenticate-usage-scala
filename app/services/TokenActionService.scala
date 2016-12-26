package services
import constants.TokenActionKey.Type
import generated.Tables._

/**
  * Service interface definition for a TokenAction. This should be the only
  * point of contact and dependency for doing TokenAction operations in the
  * rest of the application outside the dao and services packages.
  */
trait TokenActionService {
  //------------------------------------------------------------------------
  def create(user: UserRow, `type`: Type, token: String): TokenActionRow

  //------------------------------------------------------------------------
  def findByToken(token: String, `type`: Type): Option[TokenActionRow]

  //------------------------------------------------------------------------
  def isValid(tokenAction: TokenActionRow): Boolean

  //------------------------------------------------------------------------
  def targetUser(tokenAction: TokenActionRow): Option[UserRow]
}
