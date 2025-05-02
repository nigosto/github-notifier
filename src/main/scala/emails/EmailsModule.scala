package emails

import cats.effect.kernel.Resource
import cats.effect.IO
import config.SmtpConfig

case class EmailsModule(emailsService: EmailsService)

object EmailsModule:
  def apply(smtpConfig: SmtpConfig): Resource[IO, EmailsModule] = 
    val module = for 
      emailsSession <- EmailsSession(smtpConfig)
      emailsService = new EmailsService(emailsSession)
      emailsModule = new EmailsModule(emailsService)
    yield emailsModule

    Resource.eval(module)