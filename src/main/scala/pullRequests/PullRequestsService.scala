package pullRequests

import cats.effect.IO
import users.UsersService
import repositories.RepositoriesService
import entities.{PullRequest, PullRequestId, PullRequestPayload, EntityDao}
import entities.EntitiesService
import database.DatabaseTransactor
import doobie.*
import doobie.implicits.*
import doobie.postgres.sqlstate
import cats.syntax.functor.*

class PullRequestsService(
  pullRequestDao: PullRequestDao,
  usersService: UsersService,
  repositoriesService: RepositoriesService
) extends EntitiesService(pullRequestDao):
  def findOrCreate(pullRequestPayload: PullRequestPayload) = for
    author <- usersService.findOrCreateSingle(pullRequestPayload.user.toUser)
    repository <- repositoriesService.findOrCreate(pullRequestPayload.base.repo)
    pullRequest <- findOrCreateSingle(pullRequestPayload.toPullRequest)
  yield pullRequest

class PullRequestDao(transactor: DatabaseTransactor) extends EntityDao[PullRequest]("pull_requests")(transactor)