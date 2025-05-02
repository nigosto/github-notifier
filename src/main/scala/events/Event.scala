package events

import cats.syntax.all.*
import cats.effect.IO
import entities.*
import io.circe.Codec
import sttp.tapir.{Schema, SchemaType}
import doobie.util.fragment.Fragment
import doobie.implicits.*
import utils.CirceUtils
import java.time.LocalDateTime
import doobie.util.Read
import java.sql.Timestamp
import doobie.implicits.javasql.TimestampMeta
import doobie.util.invariant.InvalidEnum
import io.circe.derivation.ConfiguredEnumCodec
import utils.inputDataConfiguration
import history.Audit
import history.HistoryRecordProjection

sealed trait GithubEvent derives Codec, Schema:
  def repositoryId: RepositoryId
  def senderId: UserId
  def createdAt: LocalDateTime
  def insertQuery: Fragment

case class PullRequestEvent(
  id: EventId,
  action: PullRequestAction,
  pullRequestId: PullRequestId,
  repositoryId: RepositoryId,
  senderId: UserId,
  createdAt: LocalDateTime
) extends GithubEvent
    derives Codec,
      Schema:
  def insertQuery =
    Fragment.const(
      s"""INSERT INTO "github-notifier".${Audit[PullRequestEvent].table} """
    ) ++ fr"""VALUES (${id.id}, ${action.toString}, ${pullRequestId.id}, ${repositoryId.id}, ${senderId.id})"""

object PullRequestEvent:
  given Audit[PullRequestEvent] with
    def table = "pull_request_events"
    def projection = HistoryRecordProjection(
      "pullRequestEvent",
      Some("action"),
      None,
      None,
      None,
      Some("pullRequestId"),
      None,
      Some("repositoryId"),
      Some("senderId"),
      Some("createdAt")
    )

case class PushEvent(
  id: EventId,
  previousRef: String,
  repositoryId: RepositoryId,
  senderId: UserId,
  createdAt: LocalDateTime
) extends GithubEvent
    derives Codec,
      Schema:
  def insertQuery = Fragment.const(
    s"""INSERT INTO "github-notifier".${Audit[PushEvent].table} """
  ) ++ fr"""VALUES (${id.id}, $previousRef, ${repositoryId.id}, ${senderId.id})"""

object PushEvent:
  given Audit[PushEvent] with
    def table = "push_events"
    def projection = HistoryRecordProjection(
      "pushEvent",
      None,
      None,
      None,
      Some("previousRef"),
      None,
      None,
      Some("repositoryId"),
      Some("senderId"),
      Some("createdAt")
    )

case class IssueEvent(
  id: EventId,
  action: IssueAction,
  issueId: IssueId,
  repositoryId: RepositoryId,
  senderId: UserId,
  createdAt: LocalDateTime
) extends GithubEvent
    derives Codec,
      Schema:
  def insertQuery = Fragment.const(
    s"""INSERT INTO "github-notifier".${Audit[IssueEvent].table} """
  ) ++ fr"""VALUES (${id.id}, ${action.toString}, ${issueId.id}, ${repositoryId.id}, ${senderId.id})"""

object IssueEvent:
  given Audit[IssueEvent] with
    def table = "issue_events"
    def projection = HistoryRecordProjection(
      "issueEvent",
      Some("action"),
      None,
      None,
      None,
      None,
      Some("issueId"),
      Some("repositoryId"),
      Some("senderId"),
      Some("createdAt")
    )

case class CreateEvent(
  id: EventId,
  ref: String,
  refType: RefType,
  repositoryId: RepositoryId,
  senderId: UserId,
  createdAt: LocalDateTime
) extends GithubEvent
    derives Codec,
      Schema:
  def insertQuery = Fragment.const(
    s"""INSERT INTO "github-notifier".${Audit[CreateEvent].table} """
  ) ++ fr"""VALUES (${id.id}, $ref, ${refType.toString}, ${repositoryId.id}, ${senderId.id})"""

object CreateEvent:
  given Audit[CreateEvent] with
    def table = "create_events"
    def projection = HistoryRecordProjection(
      "createEvent",
      None,
      Some("ref"),
      Some("refType"),
      None,
      None,
      None,
      Some("repositoryId"),
      Some("senderId"),
      Some("createdAt")
    )
  
enum RefType derives ConfiguredEnumCodec:
  case Tag
  case Branch

object RefType:
  def fromString(value: String) = value match
    case "Tag" => Some(Tag)
    case "Branch" => Some(Branch)
    case _ => None

  given Read[RefType] =
    Read[String].map: s =>
      RefType
        .fromString(s)
        .getOrElse:
          throw InvalidEnum[RefType](s)

  given Schema[RefType] = Schema.derivedEnumeration()

case class EventId(id: String) extends AnyVal

object EventId:
  given Codec[EventId] = CirceUtils.unwrappedCodec(EventId.apply)(_.id)
  given Schema[EventId] = Schema(SchemaType.SString())
  given Read[EventId] = Read[String].map(EventId.apply)

  def generate: IO[EventId] = IO.randomUUID.map(uuid => EventId.apply(uuid.toString))
