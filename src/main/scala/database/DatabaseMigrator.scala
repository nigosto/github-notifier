package database

import config.DatabaseConfig
import cats.effect.IO
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.{CleanResult, MigrateResult}

class DatabaseMigrator(databaseConfig: DatabaseConfig, migrationsLocation: String):
  private val flyway: Flyway = Flyway
    .configure()
    .dataSource(databaseConfig.jdbcUrl, databaseConfig.user, databaseConfig.password)
    .schemas(databaseConfig.schema)
    .locations(migrationsLocation)
    .table("flyway_schema_history")
    .baselineOnMigrate(true)
    .load()

  def migrate(): IO[MigrateResult] = IO.blocking(flyway.migrate())

  def clean(): IO[CleanResult] = IO.blocking(flyway.clean())
