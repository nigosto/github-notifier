package emails

case class Email(
  from: String,
  to: String,
  content: EmailContent
)

case class EmailContent(
  subject: String,
  body: String
)

trait EmailMessage:
  def toEmailContent: EmailContent

  def toEmail(from: String, to: String): Email =
    Email(from, to, toEmailContent)
