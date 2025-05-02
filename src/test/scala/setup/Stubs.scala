package setup

import entities.*
import events.*
import java.time.LocalDateTime

object Stubs:
  val userStub = User(UserId(1), "test-user", Some("test-email@gmail.com"), "test-avatar-url.com")
  val userWithoutEmailStub = User(UserId(2), "test-user-no-email", None, "test-avatar-url.com")
  val newUserStub = User(UserId(3), "new-user", Some("new-user@gmail.com"), "test-avatar-url.com")
  val userForEventStub = User(UserId(4), "test-user", Some("test-email@gmail.com"), "test-avatar-url.com")

  val userPayloadStub = UserPayload(UserId(1), "test-user", Some("test-email@gmail.com"), "test-avatar-url.com")
  val userWithoutEmailPayloadStub = UserPayload(UserId(2), "test-user-no-email", None, "test-avatar-url.com")
  val userPayloadForEventStub = UserPayload(UserId(4), "test-user", Some("test-email@gmail.com"), "test-avatar-url.com")

  val invalidUserId = UserId(5)

  val repositoryStub = Repository(RepositoryId(1), "test-repo", UserId(1), "description")
  val newRepositoryStub = Repository(RepositoryId(2), "new-repo", UserId(1), "description")
  val newRepositoryWithoutOwnerStub = Repository(RepositoryId(3), "new-repo2", UserId(2), "description")
  val repositoryForEventStub = Repository(RepositoryId(4), "test-repo", UserId(4), "description")

  val repositoryPayloadStub = RepositoryPayload(RepositoryId(1), "test-repo", userPayloadStub, Some("description"))
  val newRepositoryPayloadStub = RepositoryPayload(RepositoryId(2), "new-repo", userPayloadStub, Some("description"))
  val newRepositoryPayloadWithoutOwnerStub =
    RepositoryPayload(RepositoryId(3), "new-repo2", userWithoutEmailPayloadStub, Some("description"))
  val repositoryPayloadForEventStub = RepositoryPayload(RepositoryId(4), "test-repo", userPayloadForEventStub, Some("description"))

  val invalidRepositoryId = RepositoryId(5)

  val pullRequestStub = PullRequest(PullRequestId(1), "PR", "", repositoryForEventStub.id, userForEventStub.id)
  val pullRequestForEventStub = PullRequest(PullRequestId(2), "PR", "", repositoryStub.id, userStub.id)

  val pullRequestPayloadStub =
    PullRequestPayload(PullRequestId(1), "PR", None, Base(repositoryPayloadForEventStub), userPayloadForEventStub)
  val pullRequestPayloadForEventStub =
    PullRequestPayload(PullRequestId(2), "PR", None, Base(repositoryPayloadStub), userPayloadStub)
  val incomingPullRequestEventStub =
    IncomingPullRequestEvent(PullRequestAction.Opened, pullRequestPayloadStub, repositoryPayloadForEventStub, userPayloadForEventStub)

  def createPullRequestEventStub(id: EventId, date: LocalDateTime) = PullRequestEvent(
    id,
    PullRequestAction.Opened,
    pullRequestForEventStub.id,
    repositoryStub.id,
    userStub.id,
    date
  )

  def createPushEvent(id: EventId, date: LocalDateTime) = PushEvent(
    id,
    "asd416as63d41a1sd6a",
    repositoryStub.id,
    userStub.id,
    date
  )