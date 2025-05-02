package services

import setup.TestDatabaseSetup
import org.scalamock.scalatest.AsyncMockFactory
import events.{EventDao, EventsService}
import users.{UserDao, UsersService}
import repositories.{RepositoryDao, RepositoriesService}
import pullRequests.{PullRequestDao, PullRequestsService}
import issues.{IssueDao, IssuesService}
import emails.{EmailsService, EmailsSession}
import setup.Stubs.*
import org.scalamock.function.FunctionAdapter3
import emails.EmailContent
import emails.EmailMessage
import cats.effect.IO
import events.PullRequestEvent

class EventsServiceTest extends TestDatabaseSetup with AsyncMockFactory:
  private val mockEmailsService = mock[EmailsService]

  private lazy val eventDao = new EventDao(transactor)
  private lazy val userDao = new UserDao(transactor)
  private lazy val repositoryDao = new RepositoryDao(transactor)
  private lazy val pullRequestDao = new PullRequestDao(transactor)
  private lazy val issueDao = new IssueDao(transactor)
  private lazy val usersService = new UsersService(userDao)
  private lazy val repositoriesService = new RepositoriesService(repositoryDao, usersService)
  private lazy val pullRequestsService = new PullRequestsService(pullRequestDao, usersService, repositoriesService)
  private lazy val issuesService = new IssuesService(issueDao, usersService, repositoriesService)
  private lazy val eventsService = new EventsService(
    eventDao,
    usersService,
    repositoriesService,
    pullRequestsService,
    issuesService,
    mockEmailsService
  )

  override def afterAllWithTeardown =
    eventDao.deleteAll >> pullRequestDao.deleteAll >> issueDao.deleteAll >> repositoryDao.deleteAll >> userDao.deleteAll

  "EventsService" - {
    "logEvent" - {
      "logs the event, creating all missing entities in the process" in {
        eventsService
          .logEvent(incomingPullRequestEventStub)
          .asserting(event =>
            event should matchPattern:
              case e: PullRequestEvent =>
          ) >> userDao
          .find(incomingPullRequestEventStub.sender.id.id)
          .asserting(_ shouldBe Some(userForEventStub)) >> repositoryDao
          .find(incomingPullRequestEventStub.repository.id.id)
          .asserting(_ shouldBe Some(repositoryForEventStub)) >> pullRequestDao
          .find(incomingPullRequestEventStub.pullRequest.id.id)
          .asserting(_ shouldBe Some(pullRequestStub))
      }
    }

    "notifyUsers" - {
      "sends email about the event to only those passed users, that have emails" in {
        (mockEmailsService
          .sendEmail(_, _))
          .expects(userStub.email.get, incomingPullRequestEventStub)
          .returning(IO.unit)

        (mockEmailsService
          .sendEmail(_, _))
          .expects(newUserStub.email.get, incomingPullRequestEventStub)
          .returning(IO.unit)

        eventsService
          .notifyUsers(incomingPullRequestEventStub, List(userStub, userWithoutEmailStub, newUserStub))
          .assertNoException
      }
    }

    "logAndNotify" - {
      "logs the event and sends email to those users, who have email" in {
        (mockEmailsService
          .sendEmail(_, _))
          .expects(userStub.email.get, incomingPullRequestEventStub)
          .returning(IO.unit)

        (mockEmailsService
          .sendEmail(_, _))
          .expects(newUserStub.email.get, incomingPullRequestEventStub)
          .returning(IO.unit)

        eventsService
          .logAndNotify(incomingPullRequestEventStub, List(userStub, userWithoutEmailStub, newUserStub))
          .asserting(event =>
            event should matchPattern:
              case e: PullRequestEvent =>
          ) >> userDao
          .find(incomingPullRequestEventStub.sender.id.id)
          .asserting(_ shouldBe Some(userForEventStub)) >> repositoryDao
          .find(incomingPullRequestEventStub.repository.id.id)
          .asserting(_ shouldBe Some(repositoryForEventStub)) >> pullRequestDao
          .find(incomingPullRequestEventStub.pullRequest.id.id)
          .asserting(_ shouldBe Some(pullRequestStub))
      }
    }
  }
