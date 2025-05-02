package history

import entities.*
import java.time.LocalDateTime
import io.circe.Codec
import sttp.tapir.Schema
import io.circe.derivation.ConfiguredEnumCodec
import utils.outputDataConfiguration
import doobie.util.Read
import doobie.util.invariant.InvalidEnum
import io.circe.Encoder
import utils.CirceUtils.dropNulls
import io.circe.generic.semiauto.deriveEncoder
import io.circe.Decoder
import events.EventId

// TODO: instead of ids use regular objects and fetch them with joins
case class HistoryRecord(
  id: EventId,
  `type`: EventType,
  action: Option[String],
  ref: Option[String],
  refType: Option[String],
  previousRef: Option[String],
  pullRequestId: Option[PullRequestId],
  issueId: Option[IssueId],
  repositoryId: RepositoryId,
  senderId: UserId,
  createdAt: LocalDateTime
) derives Decoder, Schema

object HistoryRecord:
  given Encoder[HistoryRecord] = dropNulls(deriveEncoder)

enum EventType derives ConfiguredEnumCodec:
  case PullRequestEvent
  case PushEvent
  case IssueEvent
  case CreateEvent

object EventType:
  def fromString(value: String) = value match
    case "pullRequestEvent" => Some(PullRequestEvent)
    case "pushEvent" => Some(PushEvent)
    case "issueEvent" => Some(IssueEvent)
    case "createEvent" => Some(CreateEvent)
    case _ => None

  given Read[EventType] =
    Read[String].map: s =>
      EventType
        .fromString(s)
        .getOrElse:
          throw InvalidEnum[EventType](s)

  given Schema[EventType] = Schema.derivedEnumeration()
