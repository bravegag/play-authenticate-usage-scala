package controllers

object TokenAction extends Enumeration {
  type Type = Value
  val EMAIL_VERIFICATION = Value("EV")
  val PASSWORD_RESET = Value("PR")
}