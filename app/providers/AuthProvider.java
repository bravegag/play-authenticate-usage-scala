package providers;

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.mail.Mailer.MailerFactory;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.*;
import controllers.routes;
import generated.Tables;
import play.Logger;
import play.data.Form;
import play.i18n.Lang;
import play.i18n.Messages;
import play.inject.ApplicationLifecycle;
import play.mvc.Call;
import play.mvc.Http.Context;
import scala.collection.JavaConversions;
import services.*;
import views.form.*;
import dao.TokenAction;

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
            PlayAuthenticate auth,
            ApplicationLifecycle lifecycle,
            UserService userService,
            TokenActionService tokenActionService,
            MailerFactory mailerFactory,
            LoginSignupFormFactory formFactory) {
        super(auth, lifecycle, mailerFactory);
        this.loginForm = formFactory.getLoginForm();
        this.signupForm = formFactory.getSignupForm();
        this.userService = userService;
        this.tokenActionService = tokenActionService;
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
    public void sendPasswordResetMailing(Tables.UserRow user, Context ctx) {
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
    public void sendVerifyEmailMailingAfterSignup(Tables.UserRow user,
                                                  Context ctx) {

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
    protected SignupResult signupUser(SecuredUserSignupAuth signupAuthUser) {
        final Tables.UserRow user = userService.findByAuthUser(signupAuthUser).get();
        if (user != null) {
            if (user.emailValidated()) {
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
        final Tables.UserRow newUser = userService.create(signupAuthUser);
        // Usually the email should be verified before allowing login, however
        // if you return
        // return SignupResult.USER_CREATED;
        // then the user gets logged in directly
        return SignupResult.USER_CREATED_UNVERIFIED;
    }

    //-------------------------------------------------------------------
    @Override
    protected LoginResult loginUser(SecuredUserLoginAuth authUser) {
        final Tables.UserRow user = userService.findByAuthUser(authUser).get();
        if (user == null) {
            return LoginResult.NOT_FOUND;
        } else {
            if (!user.emailValidated()) {
                return LoginResult.USER_UNVERIFIED;
            } else {
                List<Tables.LinkedAccountRow> linkedAccounts = JavaConversions.seqAsJavaList(userService.linkedAccounts(user));
                for (final Tables.LinkedAccountRow linkedAccount : linkedAccounts) {
                    if (getKey().equals(linkedAccount.providerKey())) {
                        if (authUser.checkPassword(linkedAccount.providerPassword(),
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
    protected Call userExists(UsernamePasswordAuthUser authUser) {
        return routes.Signup.exists();
    }

    //-------------------------------------------------------------------
    @Override
    protected Call userUnverified(UsernamePasswordAuthUser authUser) {
        return routes.Signup.unverified();
    }

    //-------------------------------------------------------------------
    @Override
    protected SecuredUserSignupAuth buildSignupAuthUser(Signup signup,
                                                        Context ctx) {
        return new SecuredUserSignupAuth(signup);
    }

    //-------------------------------------------------------------------
    @Override
    protected SecuredUserLoginAuth buildLoginAuthUser(Login login, Context ctx) {
        return new SecuredUserLoginAuth(login.getPassword(), login.getEmail());
    }


    //-------------------------------------------------------------------
    @Override
    protected SecuredUserLoginAuth transformAuthUser(SecuredUserSignupAuth authUser,
                                                     Context context) {
        return new SecuredUserLoginAuth(authUser.getEmail());
    }

    //-------------------------------------------------------------------
    @Override
    protected String getVerifyEmailMailingSubject(SecuredUserSignupAuth user, Context ctx) {
        return Messages.get("playauthenticate.password.verify_signup.subject");
    }

    //-------------------------------------------------------------------
    @Override
    protected String onLoginUserNotFound(Context context) {
        context.flash().put(controllers.Application.FLASH_ERROR_KEY(),
                Messages.get("playauthenticate.password.login.unknown_user_or_pw"));
        return super.onLoginUserNotFound(context);
    }

    //-------------------------------------------------------------------
    @Override
    protected Body getVerifyEmailMailingBody(String token,
                                             SecuredUserSignupAuth user, Context ctx) {
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
    protected String generateVerificationRecord(SecuredUserSignupAuth user) {
        return generateVerificationRecord(userService.findByAuthUser(user).get());
    }

    //-------------------------------------------------------------------
    protected String generateVerificationRecord(Tables.UserRow user) {
        final String token = generateToken();
        // Do database actions, etc.
        tokenActionService.create(user, TokenAction.EMAIL_VERIFICATION(), token);
        return token;
    }

    //-------------------------------------------------------------------
    protected String generatePasswordResetRecord(Tables.UserRow user) {
        final String token = generateToken();
        tokenActionService.create(user, TokenAction.PASSWORD_RESET(), token);
        return token;
    }

    //-------------------------------------------------------------------
    protected String getPasswordResetMailingSubject(Tables.UserRow user,
                                                    Context ctx) {
        return Messages.get("playauthenticate.password.reset_email.subject");
    }

    //-------------------------------------------------------------------
    protected Body getPasswordResetMailingBody(String token, Tables.UserRow user,
                                               Context ctx) {

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
    protected String getVerifyEmailMailingSubjectAfterSignup(Tables.UserRow user,
                                                             Context ctx) {
        return Messages.get("playauthenticate.password.verify_email.subject");
    }

    //-------------------------------------------------------------------
    protected String getEmailTemplate(String template,
                                      String langCode, String url, String token,
                                      String name, String email) {
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
    protected Body getVerifyEmailMailingBodyAfterSignup(String token,
                                                        Tables.UserRow user, Context ctx) {

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
    private String getEmailName(Tables.UserRow user) {
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
    private final UserService userService;
    private final TokenActionService tokenActionService;
}
