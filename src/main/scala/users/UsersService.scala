package users

import cats.effect.IO
import entities.{UserId, UserPayload, User, EntityDao}
import entities.EntitiesService
import database.DatabaseTransactor
import doobie.*
import doobie.implicits.*
import doobie.postgres.sqlstate
import cats.syntax.functor.*

class UsersService(userDao: UserDao) extends EntitiesService(userDao):
  def find(id: UserId) = userDao.find(id.id)

  def updateEmail(id: UserId, email: String) =
    userDao.updateEmail(id, email)

class UserDao(transactor: DatabaseTransactor) extends EntityDao[User]("users")(transactor):
  def updateEmail(id: UserId, email: String) =
    (Fragment.const(s"""UPDATE "github-notifier".$table """) ++
      fr"""
      SET email = $email
      WHERE id = ${id.id}
    """).update.run.transact(transactor)