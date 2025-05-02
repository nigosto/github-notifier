package emails

import javax.mail.{Session, Message, Transport}
import javax.mail.internet.{MimeMessage, InternetAddress}
import scala.util.{Try, Success, Failure}
import cats.effect.IO
import io.circe.Codec
import sttp.tapir.Schema

case class EmailNotSendError(message: String) extends Throwable derives Codec, Schema

class EmailsService(session: EmailsSession):
  // Wrapping the method in IO.blocking, because it contains side effects
  // and is a thread blocking operation
  def sendEmail(to: String, value: EmailMessage): IO[Unit] =
    IO.blocking {
      val message = new MimeMessage(session.session)
      val email = value.toEmail(session.sender, to)
      message.setFrom(new InternetAddress(email.from))
      message.setRecipients(Message.RecipientType.TO, email.to)
      message.setSubject(email.content.subject)
      message.setText(email.content.body)

      Transport.send(message)
    }.handleErrorWith(_ => IO.raiseError(EmailNotSendError("Could not send email")))
