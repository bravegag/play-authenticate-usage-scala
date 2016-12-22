package views.form

import play.data.{Form, FormFactory}
import javax.inject.Inject
import javax.inject.Singleton;

@Singleton
class LoginSignupFormFactory @Inject() (formFactory: FormFactory) {
    //-------------------------------------------------------------------
    // public
    //-------------------------------------------------------------------
    def getLoginForm() = loginForm

    //-------------------------------------------------------------------
    def getSignupForm() = signupForm

    //-------------------------------------------------------------------
    // members
    //-------------------------------------------------------------------
    private lazy val loginForm = formFactory.form(classOf[Login])
    private lazy val signupForm = formFactory.form(classOf[Signup])
}