package pullRequests

import database.DatabaseTransactor
import cats.effect.kernel.Resource
import cats.effect.IO
import users.UsersService
import repositories.RepositoriesService

case class PullRequstsModule(pullRequestsService: PullRequestsService)

object PullRequstsModule:
  def apply(transactor: DatabaseTransactor, usersService: UsersService, repositoriesService: RepositoriesService)
    : Resource[IO, PullRequstsModule] =
    val pullRequestDao = new PullRequestDao(transactor)
    val pullRequestsService = new PullRequestsService(pullRequestDao, usersService, repositoriesService)

    Resource.pure(new PullRequstsModule(pullRequestsService))
