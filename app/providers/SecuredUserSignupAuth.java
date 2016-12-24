package providers;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;
import views.form.Signup;

public class SecuredUserSignupAuth extends UsernamePasswordAuthUser implements NameIdentity {
	//-------------------------------------------------------------------
	// public
	//-------------------------------------------------------------------
	/**
	 * Sign up a new user
	 * @param signup form data
	 */
	public SecuredUserSignupAuth(final Signup signup) {
		super(signup.getPassword(), signup.getEmail());
		this.name = signup.name();
	}

	//-------------------------------------------------------------------
	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public SecuredUserSignupAuth(final String password) {
		super(password, null);
		name = null;
	}

	//-------------------------------------------------------------------
	@Override
	public String getName() {
		return name;
	}

	//-------------------------------------------------------------------
	// members
	//-------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	private final String name;
}
