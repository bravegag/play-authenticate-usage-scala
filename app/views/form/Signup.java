package views.form;

import play.data.validation.Constraints.*;
import play.i18n.Messages;

public class Signup extends Login {
    //-------------------------------------------------------------------
    // public
    //-------------------------------------------------------------------
    public String validate() {
        if (password == null || !password.equals(repeatPassword)) {
            return Messages.get("playauthenticate.password.signup.error.passwords_not_same");
        }
        return null;
    }

    //-------------------------------------------------------------------
    public String getRepeatPassword() {
        return repeatPassword;
    }

    //-------------------------------------------------------------------
    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    //-------------------------------------------------------------------
    public String getUsername() {
        return username;
    }

    //-------------------------------------------------------------------
    public void setUsername(String username) {
        this.username = username;
    }

    //-------------------------------------------------------------------
    // members
    //-------------------------------------------------------------------
    @Required
    @MinLength(5)
    private String repeatPassword;

    @Required
    private String username;

}