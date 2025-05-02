package events

import emails.EmailsService
import users.UsersService
import repositories.RepositoriesService
import pullRequests.PullRequestsService
import cats.effect.IO
import cats.syntax.all.*
import entities.{User, RepositoryId}
import doobie.util.Read
import doobie.postgres.implicits.JavaTimeLocalDateTimeMeta
import issues.IssuesService
import history.HistoryRecord

class EventsService(
  eventDao: EventDao,
  usersService: UsersService,
  repositoriesService: RepositoriesService,
  pullRequestsService: PullRequestsService,
  issuesService: IssuesService,
  emailsService: EmailsService
):
  def logAndNotify(incomingEvent: IncomingGithubEvent, users: List[User]) =
    logEvent(incomingEvent) <* notifyUsers(incomingEvent, users)

  def logEvent(incomingEvent: IncomingGithubEvent) =
    incomingEvent.parse(usersService, repositoriesService, pullRequestsService, issuesService) >>= eventDao.create

  def notifyUsers(incomingEvent: IncomingGithubEvent, users: List[User]) =
    users
      .collect(user =>
        user.email match
          case Some(email) => email
      )
      .parTraverse:
        emailsService
          .sendEmail(_, incomingEvent)
          .start
