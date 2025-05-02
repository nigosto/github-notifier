package config

import com.comcast.ip4s.{Host, Port}

case class HttpConfig(
  host: Host,
  port: Port
)
