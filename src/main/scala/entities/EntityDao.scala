package entities

import database.DatabaseTransactor
import doobie.*
import doobie.implicits.*
import doobie.postgres.sqlstate
import cats.syntax.functor.*

trait EntityDao[E <: Entity : Read](val table: String)(transactor: DatabaseTransactor):
  def create(entity: E) =
    (Fragment.const(s"""INSERT INTO "github-notifier".$table """) ++ entity.insertValues).update.run
      .as(entity)
      .transact(transactor)

  def find(id: Long) =
    (Fragment.const(s"""SELECT * FROM "github-notifier".$table """) ++ fr"""WHERE id = $id""")
      .query[E]
      .option
      .transact(transactor)

  def deleteAll = Fragment
    .const(s"""
      DELETE FROM "github-notifier".$table
    """)
    .update
    .run
    .transact(transactor)
