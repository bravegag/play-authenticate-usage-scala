package views.form

import javax.inject.{Inject, Singleton}

/**
  * Simplifies access to all available Dao implementations so that they don't have to
  * be injected one by one and therefore, reduces the otherwise injection cluttering.
  * @param acceptForm
  * @param forgotPasswordForm
  * @param loginForm
  * @param passwordChangeForm
  * @param passwordResetForm
  * @param signupForm
  */
@Singleton
class FormContext @Inject() (val acceptForm: AcceptForm
                           , val forgotPasswordForm: ForgotPasswordForm
                           , val loginForm: LoginForm
                           , val passwordChangeForm: PasswordChangeForm
                           , val passwordResetForm: PasswordResetForm
                           , val signupForm: SignupForm)
