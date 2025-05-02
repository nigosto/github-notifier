package services

import setup.TestDatabaseSetup
import users.UserDao
import repositories.RepositoryDao
import pullRequests.PullRequestDao
import events.{EventDao, EventId}
import history.{HistoryService, EventType}
import setup.Stubs.*
import cats.effect.IO
import java.time.LocalDateTime
import cats.syntax.all.*

class HistoryServiceTest extends TestDatabaseSetup:
  private lazy val userDao = new UserDao(transactor)
  private lazy val repositoryDao = new RepositoryDao(transactor)
  private lazy val pullRequestDao = new PullRequestDao(transactor)
  private lazy val eventDao = new EventDao(transactor)
  private lazy val historyService = new HistoryService(eventDao)

  override def beforeAllWithSetup =
    userDao.create(userStub) >> repositoryDao.create(repositoryStub) >> pullRequestDao.create(
      pullRequestForEventStub
    ) >>
      EventId.generate
        .flatMap(event => eventDao.create(createPullRequestEventStub(event, LocalDateTime.now())))
        .replicateA(3) >>
      EventId.generate
        .flatMap(event => eventDao.create(createPushEvent(event, LocalDateTime.now())))
        .replicateA(10) >>
      EventId.generate
        .flatMap(event => eventDao.create(createPullRequestEventStub(event, LocalDateTime.now())))
        .replicateA(10)

  override def afterAllWithTeardown =
    eventDao.deleteAll >> pullRequestDao.deleteAll >> repositoryDao.deleteAll >> userDao.deleteAll

  "HistoryService" - {
    "getPaginatedHistoryRecords" - {
      "returns the latest 10 history records when page = 1 and size = 10" in {
        historyService
          .getPaginatedHistoryRecords(1, 10, repositoryStub.id)
          .map(_.map(event => (event.`type`, event.repositoryId, event.senderId)))
          .asserting:
            _ should contain theSameElementsAs List.fill(10)(
              (EventType.PullRequestEvent, repositoryStub.id, userStub.id)
            )
      }

      "returns the intermidiate events when on second page" in {
        historyService
          .getPaginatedHistoryRecords(2, 10, repositoryStub.id)
          .map(_.map(event => (event.`type`, event.repositoryId, event.senderId)))
          .asserting:
            _ should contain theSameElementsAs List.fill(10)((EventType.PushEvent, repositoryStub.id, userStub.id))
      }

      "returns the oldest remaining history records when on last page" in {
        historyService
          .getPaginatedHistoryRecords(3, 10, repositoryStub.id)
          .map(_.map(event => (event.`type`, event.repositoryId, event.senderId)))
          .asserting:
            _ should contain theSameElementsAs List.fill(3)(
              (EventType.PullRequestEvent, repositoryStub.id, userStub.id)
            )
      }

      "returns empty list if there are no more events on the requested page" in {
        historyService
          .getPaginatedHistoryRecords(4, 10, repositoryStub.id)
          .asserting(_ shouldBe List.empty)
      }

      "returns empty list if page or size are invalid" in {
        historyService
          .getPaginatedHistoryRecords(-1, 10, repositoryStub.id)
          .asserting(_ shouldBe List.empty) >> historyService
          .getPaginatedHistoryRecords(2, -2, repositoryStub.id)
          .asserting(_ shouldBe List.empty)
      }
    }
  }
