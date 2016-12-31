package views.form

import javax.inject.{Inject, Singleton}

@Singleton
class FormContext @Inject() (val acceptForm: AcceptForm
                           , val forgotPasswordForm: ForgotPasswordForm
                           , val loginForm: LoginForm
                           , val passwordChangeForm: PasswordChangeForm
                           , val passwordResetForm: PasswordResetForm
                           , val signupForm: SignupForm)
