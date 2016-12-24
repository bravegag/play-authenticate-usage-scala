package constants

object TokenActionKey extends Enumeration {
  type Type = Value
  val EMAIL_VERIFICATION = Value("EV")
  val PASSWORD_RESET = Value("PR")
}