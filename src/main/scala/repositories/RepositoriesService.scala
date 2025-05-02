package repositories

import cats.effect.IO
import users.UsersService
import entities.{Repository, RepositoryId, RepositoryPayload, EntityDao}
import entities.EntitiesService
import database.DatabaseTransactor
import doobie.*
import doobie.implicits.*
import doobie.postgres.sqlstate
import cats.syntax.functor.*

class RepositoriesService(repositoryDao: RepositoryDao, usersService: UsersService) extends EntitiesService(repositoryDao):
  def find(id: RepositoryId) = repositoryDao.find(id.id)

  def findOrCreate(repositoryPayload: RepositoryPayload) = for
    owner <- usersService.findOrCreateSingle(repositoryPayload.owner.toUser)
    repository <- findOrCreateSingle(repositoryPayload.toRepository)
  yield repository

class RepositoryDao(transactor: DatabaseTransactor) extends EntityDao[Repository]("repositories")(transactor)