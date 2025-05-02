package config

import com.typesafe.config.Config
import com.comcast.ip4s.{Host, host, Port, port}

case class AppConfig(
  http: HttpConfig,
  database: DatabaseConfig,
  testDatabase: DatabaseConfig,
  smtp: SmtpConfig,
  webhookSecret: String,
  clientId: String
)

object AppConfig:
  def fromConfig(config: Config) =
    val httpConfig = config.getConfig("http")
    val http = HttpConfig(
      Host.fromString(httpConfig.getString("host")).getOrElse(host"0.0.0.0"),
      Port.fromInt(httpConfig.getInt("port")).getOrElse(port"8080")
    )

    val databaseConfig = config.getConfig("database")
    val database = DatabaseConfig(
      databaseConfig.getString("host"),
      databaseConfig.getInt("port"),
      databaseConfig.getString("user"),
      databaseConfig.getString("password"),
      databaseConfig.getString("name"),
      databaseConfig.getString("schema"),
      databaseConfig.getInt("connectionPoolSize")
    )

    val testDatabaseConfig = config.getConfig("testDatabase")
    val testDatabase = DatabaseConfig(
      testDatabaseConfig.getString("host"),
      testDatabaseConfig.getInt("port"),
      testDatabaseConfig.getString("user"),
      testDatabaseConfig.getString("password"),
      testDatabaseConfig.getString("name"),
      testDatabaseConfig.getString("schema"),
      testDatabaseConfig.getInt("connectionPoolSize")
    )

    val smtpConfig = config.getConfig("smtp")
    val smtp = SmtpConfig(
      smtpConfig.getString("host"),
      smtpConfig.getString("port"),
      smtpConfig.getString("auth"),
      smtpConfig.getString("startTlsEnable"),
      smtpConfig.getString("username"),
      smtpConfig.getString("password"),
      smtpConfig.getString("sslTrust"),
      smtpConfig.getString("sslProtocols")      
    )

    val webhookSecret = config.getString("webhookSecret")
    val clientId = config.getString("clientId")
    AppConfig(http, database, testDatabase, smtp, webhookSecret, clientId)