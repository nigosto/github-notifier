package issues

import entities.*
import users.UsersService
import cats.effect.IO
import repositories.RepositoriesService
import database.DatabaseTransactor
import doobie.*
import doobie.implicits.*
import doobie.postgres.sqlstate
import cats.syntax.functor.*

class IssuesService(
  issueDao: IssueDao,
  usersService: UsersService,
  repositoriesService: RepositoriesService
) extends EntitiesService(issueDao):
  def findOrCreate(issuePayload: IssuePayload, repository: RepositoryPayload) = for
    author <- usersService.findOrCreateSingle(issuePayload.user.toUser)
    repository <- repositoriesService.findOrCreate(repository)
    issue <- findOrCreateSingle(issuePayload.toIssue(repository.id))
  yield issue

class IssueDao(transactor: DatabaseTransactor) extends EntityDao[Issue]("issues")(transactor)
