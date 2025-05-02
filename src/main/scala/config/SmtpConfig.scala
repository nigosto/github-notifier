package config

case class SmtpConfig(
  host: String,
  port: String,
  auth: String,
  startTlsEnable: String,
  username: String,
  password: String,
  sslTrust: String,
  sslProtocols: String
)
