package users

import users.UsersEndpoints.*
import cats.effect.IO
import cats.syntax.all.*
import fs2.{Stream, text}
import scala.concurrent.duration.DurationInt
import org.http4s.client.Client
import org.http4s.{Request, Method, Uri}
import io.circe.generic.auto.*
import io.circe.syntax.EncoderOps
import users.preferences.PreferencesService

class UsersController(
  client: Client[IO],
  clientId: String,
  usersService: UsersService,
  preferencesService: PreferencesService
)(
  authenticationService: AuthenticationService
):
  import authenticationService.authenticate

  /*
    Note: this endpoint goes through the device flow for
    authenticating with GitHub. First it sends the code
    which the user has to enter at the specific url and
    then it polls the API until the user enters the correct
    code or an error occurs. The responses are streamed so
    that the user does not have to make several requests
    between their actions outside the application. Upon
    successful authentication, the last sent message contains
    the token, which the user should include as part of their
    following requests.
   */
  def signin = signinEndpoint.serverLogic: _ =>
    val basePath = "https://github.com/login"
    val grantType = "urn:ietf:params:oauth:grant-type:device_code"
    val deviceCodeUri = Uri.unsafeFromString(s"$basePath/device/code?client_id=$clientId")

    for
      response <- client.expect[DeviceCodeResponse](Request[IO](Method.POST, deviceCodeUri))
      userAccessTokenUri = Uri.unsafeFromString(
        s"$basePath/oauth/access_token?client_id=$clientId&device_code=${response.deviceCode}&grant_type=$grantType"
      )
      deviceCodeStream = Stream.emit(response.asJson.spaces2 :+ '\n')
      userAccessTokenStream = Stream
        .eval(pollUserAccessTokenEndpoint(userAccessTokenUri, response.interval).map(_.asJson.spaces2 :+ '\n'))
    yield Right(deviceCodeStream.merge(userAccessTokenStream).through(text.utf8.encode))

  def updateEmail = updateEmailEndpoint.authenticate
    .serverLogicSuccess((user, _) => emailPayload => usersService.updateEmail(user.id, emailPayload.email).void)

  def updatePreferences = updatePreferencesEndpoint.authenticate
    .serverLogicSuccess: (user, _) =>
      preferencesPayload =>
        preferencesService.updateByUserAndRepositoryId(
          user.id,
          preferencesPayload.repositoryId,
          preferencesPayload.preferences
        )

  val endpoints = List(signin, updateEmail, updatePreferences)

  private def pollUserAccessTokenEndpoint(userAccessTokenUri: Uri, interval: Int): IO[UserAccessTokenResponse] =
    client
      .expect[UserAccessTokenResponse](Request[IO](Method.POST, userAccessTokenUri))
      .flatMap(payload => payload.error.fold(IO.pure(payload))(error => IO.raiseError(error.toError)))
      .handleErrorWith: error =>
        error match
          case AuthorizationPendingError() =>
            IO.sleep(interval.second) >> pollUserAccessTokenEndpoint(userAccessTokenUri, interval)
          case ExpiredTokenError() => IO.pure(UserAccessTokenResponse(None, Some(UserAccessTokenError.ExpiredToken)))
          case AccessDeniedError() => IO.pure(UserAccessTokenResponse(None, Some(UserAccessTokenError.AccessDenied)))
