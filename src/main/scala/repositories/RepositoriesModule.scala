package repositories

import database.DatabaseTransactor
import cats.effect.kernel.Resource
import cats.effect.IO
import users.UsersService

case class RepositoriesModule(repositoriesService: RepositoriesService)

object RepositoriesModule:
  def apply(transactor: DatabaseTransactor, usersService: UsersService): Resource[IO, RepositoriesModule] =
    val repositoryDao = new RepositoryDao(transactor)
    val repositoriesService = new RepositoriesService(repositoryDao, usersService)

    Resource.pure(new RepositoriesModule(repositoriesService))