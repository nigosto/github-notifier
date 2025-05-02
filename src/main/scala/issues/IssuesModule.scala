package issues

import database.DatabaseTransactor
import users.UsersService
import repositories.RepositoriesService
import cats.effect.kernel.Resource
import cats.effect.IO
case class IssuesModule(issuesService: IssuesService)

object IssuesModule:
  def apply(transactor: DatabaseTransactor, usersService: UsersService, repositoriesService: RepositoriesService): Resource[IO, IssuesModule] = 
    val issueDao = new IssueDao(transactor)
    val issuesService = new IssuesService(issueDao, usersService, repositoriesService)

    Resource.pure(new IssuesModule(issuesService))
