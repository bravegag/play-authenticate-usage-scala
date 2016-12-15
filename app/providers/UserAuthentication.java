package providers;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;
import providers.AuthenticationProvider.MySignup;
import views.form.Signup;

public class UserAuthentication extends UsernamePasswordAuthUser implements NameIdentity {
	//-------------------------------------------------------------------
	// public
	//-------------------------------------------------------------------
	public UserAuthentication(final Signup signup) {
		super(signup.password(), signup.email());
		this.name = signup.username();
	}

	//-------------------------------------------------------------------
	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public UserAuthentication(final String password) {
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
