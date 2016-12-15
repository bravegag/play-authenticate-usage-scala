package providers;

import com.feth.play.module.pa.providers.password.*;

public class MyLoginUsernamePasswordAuthUser extends DefaultUsernamePasswordAuthUser {
	//-------------------------------------------------------------------
	// public
	//-------------------------------------------------------------------
	/**
	 * For logging the user in automatically
	 * @param email
	 */
	public MyLoginUsernamePasswordAuthUser(final String email) {
		this(null, email);
	}

	//-------------------------------------------------------------------
	public MyLoginUsernamePasswordAuthUser(final String clearPassword,
                                           final String email) {
		super(clearPassword, email);
		expiration = System.currentTimeMillis() + 1000 * DEFAULT_SESSION_TIMEOUT;
	}

	//-------------------------------------------------------------------
	@Override
	public long expires() {
		return expiration;
	}

	//-------------------------------------------------------------------
	// members
	//-------------------------------------------------------------------
	private static final long serialVersionUID = 1L;

	/**
	 * The session timeout in seconds (defaults to two weeks)
	 */
	final static long DEFAULT_SESSION_TIMEOUT = 24 * 14 * 3600;
	private long expiration;
}
