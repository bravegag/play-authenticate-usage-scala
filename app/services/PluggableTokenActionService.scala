package services

import generated.Tables._

trait PluggableTokenActionService {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  def isValid : Boolean

  //------------------------------------------------------------------------
  def targetUser : Option[UserRow]
}

object PluggableTokenActionService {
  implicit class toPluggableTokenActionService(tokenAction: TokenActionRow)(implicit tokenActionService: TokenActionService) extends PluggableTokenActionService {
    //------------------------------------------------------------------------
    override def isValid = {
      tokenActionService.isValid(tokenAction)
    }

    //------------------------------------------------------------------------
    override def targetUser = {
      tokenActionService.targetUser(tokenAction)
    }
  }
}
