package users.preferences

import entities.{UserId, RepositoryId}
import doobie.util.Read
import doobie.util.invariant.InvalidEnum
import sttp.tapir.Schema
import io.circe.Decoder

case class Preferences(
  userId: UserId,
  repositoryId: RepositoryId,
  preference: Preference
)

enum Preference derives Schema:
  case PullRequests
  case Pushes
  case Issues
  case Creations

object Preference:
  def fromString(str: String) = str match
    case "PullRequests" => Some(PullRequests) 
    case "Pushes" => Some(Pushes) 
    case "Issues" => Some(Issues) 
    case "Creations" => Some(Creations)   
    case _ => None

  given Read[Preference] = 
    Read[String].map: s =>
      fromString(s)
        .getOrElse:
          throw InvalidEnum[Preference](s)

  given Decoder[Preference] = 
    Decoder[String].map: s =>
      fromString(s).getOrElse:
        throw InvalidPreferenceError(s)

case class InvalidPreferenceError(preference: String) extends Throwable