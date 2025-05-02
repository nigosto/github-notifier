package events

import io.circe.Codec
import utils.{CirceUtils, inputDataConfiguration}
import sttp.tapir.Schema
import sttp.tapir.SchemaType
import io.circe.derivation.ConfiguredEnumCodec
import sttp.tapir.generic.auto.*
import io.circe.derivation.ConfiguredCodec
import entities.*
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.{deriveDecoder}
import doobie.util.Read
import doobie.util.invariant.InvalidEnum
import emails.{EmailMessage, EmailContent}
import cats.effect.IO
import users.UsersService
import repositories.RepositoriesService
import pullRequests.PullRequestsService
import cats.syntax.all.*
import users.preferences.Preference
import java.time.LocalDateTime
import issues.IssuesService

/*
  Note: there are many more types of GitHub events, but
  for now the system supports only the ones listed below.
  In case of a requirement for a new event, it should be
  added here, it should extend the IncomingGithubEvent trait and
  it should follow the naming convention, otherwise the
  JSON parsing logic will fail.
 */
sealed trait IncomingGithubEvent extends EmailMessage derives Codec, Schema:
  def repository: RepositoryPayload
  def sender: UserPayload
  def parse(
    usersService: UsersService,
    repositoriesService: RepositoriesService,
    pullRequestsService: PullRequestsService,
    issuesService: IssuesService
  ): IO[GithubEvent]
  def toEmailContent: EmailContent
  def preference: Preference

case class IncomingPullRequestEvent(
  action: PullRequestAction,
  pullRequest: PullRequestPayload,
  repository: RepositoryPayload,
  sender: UserPayload
) extends IncomingGithubEvent
    derives ConfiguredCodec,
      Schema:
  def parse(
    usersService: UsersService,
    repositoriesService: RepositoriesService,
    pullRequestsService: PullRequestsService,
    issuesService: IssuesService
  ) = (
    EventId.generate,
    // We can't run them in parallel, because there might be data races. Caught it with the tests :)
    for
      user <- usersService.findOrCreateSingle(sender.toUser)
      repository <- repositoriesService.findOrCreate(repository)
      pullRequest <- pullRequestsService.findOrCreate(pullRequest)
    yield (user.id, repository.id, pullRequest.id),
    IO(LocalDateTime.now) // considering it as a side effect, for simplicity not using OffsetDateTime
  ).parMapN:
    case (id, (userId, repositoryId, pullRequestId), createdAt) =>
      PullRequestEvent(id, action, pullRequestId, repositoryId, userId, createdAt)

  def toEmailContent =
    EmailContent(
      "New Pull Request Event",
      s"${sender.login} has ${action} in ${pullRequest.title} on ${repository.name}"
    )
  def preference: Preference = Preference.PullRequests

case class IncomingPushEvent(before: String, sender: UserPayload, repository: RepositoryPayload)
    extends IncomingGithubEvent derives Codec, Schema:
  def parse(
    usersService: UsersService,
    repositoriesService: RepositoriesService,
    pullRequestsService: PullRequestsService,
    issuesService: IssuesService
  ) = (
    EventId.generate,
    for
      repository <- repositoriesService.findOrCreate(repository)
      user <- usersService.findOrCreateSingle(sender.toUser)
    yield (repository.id, user.id),
    IO(LocalDateTime.now)
  ).parMapN:
    case (id, (repositoryId, userId), createdAt) => PushEvent(id, before, repositoryId, userId, createdAt)

  def toEmailContent = EmailContent(
    "New Push Event",
    s"${sender.login} has pushed in ${repository.name}"
  )
  def preference: Preference = Preference.Pushes

case class IncomingIssuesEvent(
  action: IssueAction,
  issue: IssuePayload,
  sender: UserPayload,
  repository: RepositoryPayload
) extends IncomingGithubEvent
    derives Codec,
      Schema:
  def parse(
    usersService: UsersService,
    repositoriesService: RepositoriesService,
    pullRequestsService: PullRequestsService,
    issuesService: IssuesService
  ) = (
    EventId.generate,
    for
      issue <- issuesService.findOrCreate(issue, repository)
      user <- usersService.findOrCreateSingle(sender.toUser)
    yield (issue.id, issue.repositoryId, user.id),
    IO(LocalDateTime.now)
  ).parMapN:
    case (id, (issueId, repositoryId, userId), createdAt) =>
      IssueEvent(id, action, issueId, repositoryId, userId, createdAt)

  def toEmailContent = EmailContent(
    "New Issue Event",
    s"${sender.login} has ${action} issue ${issue.title}"
  )

  def preference: Preference = Preference.Issues

case class IncomingCreateEvent(
  ref: String,
  refType: RefType,
  sender: UserPayload,
  repository: RepositoryPayload
) extends IncomingGithubEvent
    derives Codec,
      Schema:
  def parse(
    usersService: UsersService,
    repositoriesService: RepositoriesService,
    pullRequestsService: PullRequestsService,
    issuesService: IssuesService
  ) = (
    EventId.generate,
    for
      repository <- repositoriesService.findOrCreate(repository)
      user <- usersService.findOrCreateSingle(sender.toUser)
    yield (repository.id, user.id),
    IO(LocalDateTime.now)
  ).parMapN:
    case (id, (repositoryId, userId), createdAt) =>
      CreateEvent(id, ref, refType, repositoryId, userId, createdAt)

  def toEmailContent = EmailContent(
    "New Create Event",
    s"${sender.login} has created new $refType: $ref"
  )
  def preference: Preference = Preference.Creations

enum PullRequestAction derives ConfiguredEnumCodec:
  case Assigned
  case Closed
  case Opened
  case ReadyForReview
  case ReviewRequested

object PullRequestAction:
  def fromString(value: String) = value match
    case "Assigned" => Some(Assigned)
    case "Closed" => Some(Closed)
    case "Opened" => Some(Opened)
    case "ReadyForReview" => Some(ReadyForReview)
    case "ReviewRequested" => Some(ReviewRequested)
    case _ => None

  given Read[PullRequestAction] =
    Read[String].map: s =>
      PullRequestAction
        .fromString(s)
        .getOrElse:
          throw InvalidEnum[PullRequestAction](s)

  given Schema[PullRequestAction] = Schema.derivedEnumeration()

enum IssueAction derives ConfiguredEnumCodec:
  case Assigned
  case Closed
  case Opened
  case Reopened
  case Locked

object IssueAction:
  def fromString(value: String) = value match
    case "Assigned" => Some(Assigned)
    case "Closed" => Some(Closed)
    case "Opened" => Some(Opened)
    case "Reopened" => Some(Reopened)
    case "Locked" => Some(Locked)
    case _ => None

  given Read[IssueAction] =
    Read[String].map: s =>
      IssueAction
        .fromString(s)
        .getOrElse:
          throw InvalidEnum[IssueAction](s)

  given Schema[IssueAction] = Schema.derivedEnumeration()
