package views.form;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.*;
import play.data.validation.Constraints.*;

public class Login implements UsernamePassword {
    //-------------------------------------------------------------------
    // public
    //-------------------------------------------------------------------
    @Override
    public String getEmail() {
        return email;
    }

    //-------------------------------------------------------------------
    @Override
    public String getPassword() {
        return password;
    }

    //-------------------------------------------------------------------
    public void setEmail(String email) {
        this.email = email;
    }

    //-------------------------------------------------------------------
    public void setPassword(String password) {
        this.password = password;
    }

    //-------------------------------------------------------------------
    // members
    //-------------------------------------------------------------------
    @Required
    @Email
    public String email;

    @Required
    @MinLength(5)
    protected String password;
}
