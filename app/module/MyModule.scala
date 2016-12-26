package module

import com.feth.play.module.mail.IMailer
import com.feth.play.module.mail.Mailer
import com.feth.play.module.pa.Resolver
import com.feth.play.module.pa.providers.openid.OpenIdAuthProvider
import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import providers.{MyAuthProvider, MyResolver}
import services._

class MyModule extends AbstractModule {
  //------------------------------------------------------------------------
  // protected
  //------------------------------------------------------------------------
  protected def configure() {
    install(new FactoryModuleBuilder().implement(classOf[IMailer], classOf[Mailer]).build(classOf[Mailer.MailerFactory]))

    bind(classOf[Resolver]).to(classOf[MyResolver])
    bind(classOf[UserService]).to(classOf[UserServiceImpl]).asEagerSingleton()
    bind(classOf[TokenActionService]).to(classOf[TokenActionServiceImpl]).asEagerSingleton()
    bind(classOf[MyAuthProvider]).asEagerSingleton()
    bind(classOf[OpenIdAuthProvider]).asEagerSingleton()
  }
}