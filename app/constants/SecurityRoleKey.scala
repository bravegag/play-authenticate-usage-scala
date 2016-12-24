package constants

object SecurityRoleKey extends Enumeration {
  type Type = Value
  val USER_ROLE = Value("user")
  val ADMINISTRATOR_ROLE = Value("administrator")
}