package views.form;

import play.data.Form;
import play.data.FormFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginSignupFormFactory {
    //-------------------------------------------------------------------
    // public
    //-------------------------------------------------------------------
    @Inject
    public LoginSignupFormFactory(FormFactory formFactory) {
        this.formFactory = formFactory;
        this.loginForm = formFactory.form(Login.class);
        this.signupForm = formFactory.form(Signup.class);
    }

    //-------------------------------------------------------------------
    public Form<Login> getLoginForm() {
        return loginForm;
    }

    //-------------------------------------------------------------------
    public Form<Signup> getSignupForm() {
        return signupForm;
    }

    //-------------------------------------------------------------------
    // members
    //-------------------------------------------------------------------
    final FormFactory formFactory;
    final Form<Login> loginForm;
    final Form<Signup> signupForm;
}
