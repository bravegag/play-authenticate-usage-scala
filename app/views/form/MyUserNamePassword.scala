package views.form

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider._

trait MyUsernamePassword extends UsernamePassword {
  val email : String
  val password : String

  def getEmail = email

  def getPassword = password
}