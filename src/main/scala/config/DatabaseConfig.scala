package config

case class DatabaseConfig(
  host: String,
  port: Int,
  user: String,
  password: String,
  name: String, 
  schema: String, 
  connectionPoolSize: Int
):
  def jdbcUrl: String = s"jdbc:postgresql://$host:$port/$name"
