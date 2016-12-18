package providers;

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.mail.Mailer.MailerFactory;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import controllers.routes;
import generated.Tables;
import play.Logger;
import play.data.Form;
import play.i18n.Lang;
import play.i18n.Messages;
import play.inject.ApplicationLifecycle;
import play.mvc.Call;
import play.mvc.Http.Context;
import views.form.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class AuthProvider extends UsernamePasswordAuthProvider<String,
        SecuredUserLoginAuth, SecuredUserSignupAuth, Login, Signup> {
    //-------------------------------------------------------------------
    // public
    //-------------------------------------------------------------------
    @Inject
    public AuthProvider(
            final PlayAuthenticate auth,
            final ApplicationLifecycle lifecycle,
            MailerFactory mailerFactory,
            LoginSignupFormFactory formFactory) {
        super(auth, lifecycle, mailerFactory);
        this.loginForm = formFactory.getLoginForm();
        this.signupForm = formFactory.getSignupForm();
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
    public void sendPasswordResetMailing(final Tables.UserRow user, final Context ctx) {
        final String token = generatePasswordResetRecord(user);
        final String subject = getPasswordResetMailingSubject(user, ctx);
        final Body body = getPasswordResetMailingBody(token, user, ctx);
        sendMail(subject, body, getEmailName(user));
    }

    //-------------------------------------------------------------------
    public boolean isLoginAfterPasswordReset() {
        return getConfiguration().getBoolean(
                SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
    }

    //-------------------------------------------------------------------
    public void sendVerifyEmailMailingAfterSignup(final Tables.UserRow user,
                                                  final Context ctx) {

        final String subject = getVerifyEmailMailingSubjectAfterSignup(user,
                ctx);
        final String token = generateVerificationRecord(user);
        final Body body = getVerifyEmailMailingBodyAfterSignup(token, user, ctx);
        sendMail(subject, body, getEmailName(user));
    }

    //-------------------------------------------------------------------
    // protected
    //-------------------------------------------------------------------
    @Override
    protected List<String> neededSettingKeys() {
        final List<String> needed = new ArrayList<String>(
                super.neededSettingKeys());
        needed.add(SETTING_KEY_VERIFICATION_LINK_SECURE);
        needed.add(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
        needed.add(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
        return needed;
    }

    //-------------------------------------------------------------------
    @Override
    protected SignupResult signupUser(final SecuredUserSignupAuth user) {
        final Tables.UserRow user = User.findByUsernamePasswordIdentity(user);
        if (user != null) {
            if (Boolean.valueOf(user.emailValidated().get().toString())) {
                // This user exists, has its email validated and is active
                return SignupResult.USER_EXISTS;
            } else {
                // this user exists, is active but has not yet validated its
                // email
                return SignupResult.USER_EXISTS_UNVERIFIED;
            }
        }
        // The user either does not exist or is inactive - create a new one
        @SuppressWarnings("unused")
        final Tables.UserRow newUser = User.create(user);
        // Usually the email should be verified before allowing login, however
        // if you return
        // return SignupResult.USER_CREATED;
        // then the user gets logged in directly
        return SignupResult.USER_CREATED_UNVERIFIED;
    }

    //-------------------------------------------------------------------
    @Override
    protected LoginResult loginUser(
            final SecuredUserLoginAuth authUser) {
        final Tables.UserRow user = User.findByUsernamePasswordIdentity(authUser);
        if (user == null) {
            return LoginResult.NOT_FOUND;
        } else {
            if (!Boolean.valueOf(user.emailValidated().get().toString())) {
                return LoginResult.USER_UNVERIFIED;
            } else {
                for (final Tables.LinkedAccountRow acc : user.linkedAccounts) {
                    if (getKey().equals(acc.providerKey())) {
                        if (authUser.checkPassword(acc.providerPassword(),
                                authUser.getPassword())) {
                            // Password was correct
                            return LoginResult.USER_LOGGED_IN;
                        } else {
                            // if you don't return here,
                            // you would allow the user to have
                            // multiple passwords defined
                            // usually we don't want this
                            return LoginResult.WRONG_PASSWORD;
                        }
                    }
                }
                return LoginResult.WRONG_PASSWORD;
            }
        }
    }

    //-------------------------------------------------------------------
    @Override
    protected Call userExists(final UsernamePasswordAuthUser authUser) {
        return routes.Signup.exists();
    }

    //-------------------------------------------------------------------
    @Override
    protected Call userUnverified(final UsernamePasswordAuthUser authUser) {
        return routes.Signup.unverified();
    }

    //-------------------------------------------------------------------
    @Override
    protected SecuredUserSignupAuth buildSignupAuthUser(final Signup signup,
                                                        final Context ctx) {
        return new SecuredUserSignupAuth(signup);
    }

    //-------------------------------------------------------------------
    @Override
    protected SecuredUserLoginAuth buildLoginAuthUser(final Login login, final Context ctx) {
        return new SecuredUserLoginAuth(login.getPassword(), login.getEmail());
    }


    //-------------------------------------------------------------------
    @Override
    protected SecuredUserLoginAuth transformAuthUser(final SecuredUserSignupAuth authUser,
                                                     final Context context) {
        return new SecuredUserLoginAuth(authUser.getEmail());
    }

    //-------------------------------------------------------------------
    @Override
    protected String getVerifyEmailMailingSubject(
            final SecuredUserSignupAuth user, final Context ctx) {
        return Messages.get("playauthenticate.password.verify_signup.subject");
    }

    //-------------------------------------------------------------------
    @Override
    protected String onLoginUserNotFound(final Context context) {
        context.flash().put(controllers.Application.FLASH_ERROR_KEY(),
                Messages.get("playauthenticate.password.login.unknown_user_or_pw"));
        return super.onLoginUserNotFound(context);
    }

    //-------------------------------------------------------------------
    @Override
    protected Body getVerifyEmailMailingBody(final String token,
                                             final SecuredUserSignupAuth user, final Context ctx) {
        final boolean isSecure = getConfiguration().getBoolean(
                SETTING_KEY_VERIFICATION_LINK_SECURE);
        final String url = routes.Signup.verify(token).absoluteURL(
                ctx.request(), isSecure);

        final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
        final String langCode = lang.code();

        final String html = getEmailTemplate(
                "views.html.account.signup.email.verify_email", langCode, url,
                token, user.getName(), user.getEmail());
        final String text = getEmailTemplate(
                "views.txt.account.signup.email.verify_email", langCode, url,
                token, user.getName(), user.getEmail());

        return new Body(text, html);
    }

    //-------------------------------------------------------------------
    @Override
    protected String generateVerificationRecord(
            final SecuredUserSignupAuth user) {
        return generateVerificationRecord(User.findByAuthUserIdentity(user));
    }

    //-------------------------------------------------------------------
    protected String generateVerificationRecord(final Tables.UserRow user) {
        final String token = generateToken();
        // Do database actions, etc.
        TokenAction.create(Type.EMAIL_VERIFICATION, token, user);
        return token;
    }

    //-------------------------------------------------------------------
    protected String generatePasswordResetRecord(final Tables.UserRow user) {
        final String token = generateToken();
        TokenAction.create(Type.PASSWORD_RESET, token, user);
        return token;
    }

    //-------------------------------------------------------------------
    protected String getPasswordResetMailingSubject(final Tables.UserRow user,
                                                    final Context ctx) {
        return Messages.get("playauthenticate.password.reset_email.subject");
    }

    //-------------------------------------------------------------------
    protected Body getPasswordResetMailingBody(final String token,
                                               final Tables.UserRow user, final Context ctx) {

        final boolean isSecure = getConfiguration().getBoolean(
                SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
        final String url = routes.Signup.resetPassword(token).absoluteURL(
                ctx.request(), isSecure);

        final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
        final String langCode = lang.code();

        final String html = getEmailTemplate(
                "views.html.account.email.password_reset", langCode, url,
                token, user.username(), user.email());
        final String text = getEmailTemplate(
                "views.txt.account.email.password_reset", langCode, url, token,
                user.username(), user.email());

        return new Body(text, html);
    }

    //-------------------------------------------------------------------
    protected String getVerifyEmailMailingSubjectAfterSignup(final Tables.UserRow user,
                                                             final Context ctx) {
        return Messages.get("playauthenticate.password.verify_email.subject");
    }

    //-------------------------------------------------------------------
    protected String getEmailTemplate(final String template,
                                      final String langCode, final String url, final String token,
                                      final String name, final String email) {
        Class<?> cls = null;
        String ret = null;
        try {
            cls = Class.forName(template + "_" + langCode);
        } catch (ClassNotFoundException e) {
            Logger.warn("Template: '"
                    + template
                    + "_"
                    + langCode
                    + "' was not found! Trying to use English fallback template instead.");
        }
        if (cls == null) {
            try {
                cls = Class.forName(template + "_"
                        + EMAIL_TEMPLATE_FALLBACK_LANGUAGE);
            } catch (ClassNotFoundException e) {
                Logger.error("Fallback template: '" + template + "_"
                        + EMAIL_TEMPLATE_FALLBACK_LANGUAGE
                        + "' was not found either!");
            }
        }
        if (cls != null) {
            Method htmlRender = null;
            try {
                htmlRender = cls.getMethod("render", String.class,
                        String.class, String.class, String.class);
                ret = htmlRender.invoke(null, url, token, name, email)
                        .toString();

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    //-------------------------------------------------------------------
    protected Body getVerifyEmailMailingBodyAfterSignup(final String token,
                                                        final Tables.UserRow user, final Context ctx) {

        final boolean isSecure = getConfiguration().getBoolean(
                SETTING_KEY_VERIFICATION_LINK_SECURE);
        final String url = routes.Signup.verify(token).absoluteURL(
                ctx.request(), isSecure);

        final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
        final String langCode = lang.code();

        final String html = getEmailTemplate(
                "views.html.account.email.verify_email", langCode, url, token,
                user.username(), user.email());
        final String text = getEmailTemplate(
                "views.txt.account.email.verify_email", langCode, url, token,
                user.username(), user.email());

        return new Body(text, html);
    }

    //-------------------------------------------------------------------
    // private
    //-------------------------------------------------------------------
    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    //-------------------------------------------------------------------
    private String getEmailName(final Tables.UserRow user) {
        return getEmailName(user.email(), user.username());
    }

    //-------------------------------------------------------------------
    // members
    //-------------------------------------------------------------------
    private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL
            + "." + "verificationLink.secure";
    private static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL
            + "." + "passwordResetLink.secure";
    private static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";

    private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";

    private final Form<Signup> signupForm;
    private final Form<Login> loginForm;
}
