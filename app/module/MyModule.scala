package module

import com.feth.play.module.mail.IMailer
import com.feth.play.module.mail.Mailer
import com.feth.play.module.pa.Resolver
import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder

import service._

class MyModule extends AbstractModule {
  protected def configure() {
    install(new FactoryModuleBuilder().implement(classOf[IMailer], classOf[Mailer]).build(classOf[Mailer.MailerFactory]))
    bind(classOf[Resolver]).to(classOf[MyResolver])
  }
}
