package emails

import javax.mail.{Session, Authenticator, PasswordAuthentication}
import cats.effect.IO
import java.util.Properties
import config.SmtpConfig

case class EmailsSession(session: Session, sender: String)

object EmailsSession:
  // Wrapping the method in IO, because it contains side effects
  def apply(smtpConfig: SmtpConfig) = IO:
    val props = new Properties()
    props.put("mail.smtp.host", smtpConfig.host)
    props.put("mail.smtp.port", smtpConfig.port)
    props.put("mail.smtp.auth", smtpConfig.auth)
    props.put("mail.smtp.starttls.enable", smtpConfig.startTlsEnable)
    props.put("mail.smtp.user", smtpConfig.username)
    props.put("mail.smtp.password", smtpConfig.password)
    props.put("mail.smtp.ssl.trust", smtpConfig.sslTrust);
    props.put("mail.smtp.ssl.protocols", smtpConfig.sslProtocols);

    new EmailsSession(
      Session.getInstance(
        props,
        new Authenticator:
          override protected def getPasswordAuthentication =
            new PasswordAuthentication(smtpConfig.username, smtpConfig.password)
      ),
      smtpConfig.username
    )
