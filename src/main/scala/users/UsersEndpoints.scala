package users

import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.capabilities.Streams
import sttp.capabilities.fs2.Fs2Streams
import cats.effect.IO
import io.circe.{Codec, Json}
import io.circe.generic.auto.*
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityDecoder
import org.http4s.circe.*
import io.circe.{Encoder, Decoder}
import utils.CirceUtils.dropNulls
import io.circe.derivation.ConfiguredDecoder
import utils.inputDataConfiguration
import api.AppEndpoints.*
import users.preferences.Preference
import entities.RepositoryId

case class DeviceCodeResponse(
  deviceCode: String,
  userCode: String,
  verificationUri: String,
  interval: Int
) derives ConfiguredDecoder,
      Schema

object DeviceCodeResponse:
  given EntityDecoder[IO, DeviceCodeResponse] = jsonOf
  given Encoder[DeviceCodeResponse] =
    deriveEncoder[DeviceCodeResponse]
      .mapJsonObject(_.filterKeys(key => key != "deviceCode" && key != "interval"))

case class AuthorizationPendingError() extends Throwable
case class ExpiredTokenError() extends Throwable
case class AccessDeniedError() extends Throwable
case class InvalidUserAccessTokenError(message: String) extends Throwable

enum UserAccessTokenError:
  case AuthorizationPending
  case ExpiredToken
  case AccessDenied

object UserAccessTokenError:
  def fromString(value: String) = value match
    case "authorization_pending" => Some(AuthorizationPending)
    case "expired_token" => Some(ExpiredToken)
    case "access_denied" => Some(AccessDenied)
    case _ => None

  given Decoder[UserAccessTokenError] =
    Decoder[String].map: s =>
      fromString(s)
        .getOrElse:
          throw InvalidUserAccessTokenError(s)
  
  given Encoder[UserAccessTokenError] with
    final def apply(error: UserAccessTokenError) = Json.fromString(error.toString)

  given Schema[UserAccessTokenError] = Schema.derivedEnumeration()

  extension(error: UserAccessTokenError)
    def toError = error match
      case AuthorizationPending => AuthorizationPendingError()
      case ExpiredToken => ExpiredTokenError()
      case AccessDenied => AccessDeniedError()

case class UserAccessTokenResponse(
  accessToken: Option[String],
  error: Option[UserAccessTokenError]
) derives ConfiguredDecoder,
      Schema

object UserAccessTokenResponse:
  given EntityDecoder[IO, UserAccessTokenResponse] = jsonOf
  given Encoder[UserAccessTokenResponse] = dropNulls(deriveEncoder)

case class EmailPayload(email: String) derives Codec, Schema

case class PreferencesPayload(
  repositoryId: RepositoryId,
  preferences: List[Preference]
) derives Codec, Schema

object UsersEndpoints:
  val usersBaseEndpoint = v1BaseEndpoint.in("users")

  // TODO: fix this endpoint's schema
  val signinEndpoint = usersBaseEndpoint
    .out(streamBody(Fs2Streams[IO])(Schema.derived[DeviceCodeResponse], CodecFormat.Json()))
    .get

  val updateEmailEndpoint = usersBaseEndpoint
    .secure
    .in("email")
    .in(jsonBody[EmailPayload])
    .patch

  val updatePreferencesEndpoint = usersBaseEndpoint
    .secure
    .in("preferences")
    .in(jsonBody[PreferencesPayload])
    .patch