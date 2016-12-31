package views.form

import providers.AbstractUsernamePasswordAuthProvider.UsernamePassword

trait MyUsernamePassword extends UsernamePassword {
  val email : String
  val password : String

  def getEmail = email

  def getPassword = password
}