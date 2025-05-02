package database

import doobie.hikari.HikariTransactor
import cats.effect.IO
import config.DatabaseConfig
import cats.effect.kernel.Resource
import doobie.util.ExecutionContexts

type DatabaseTransactor = HikariTransactor[IO]

case class DatabaseModule(transactor: DatabaseTransactor)

object DatabaseModule:
  def apply(databaseConfig: DatabaseConfig): Resource[IO, DatabaseModule] = for
    _ <- Resource.eval(new DatabaseMigrator(databaseConfig, "classpath:/migrations").migrate())

    connectionEc <- ExecutionContexts.fixedThreadPool[IO](databaseConfig.connectionPoolSize)
    transactor <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      databaseConfig.jdbcUrl,
      databaseConfig.user,
      databaseConfig.password,
      connectionEc
    )
  yield DatabaseModule(transactor)