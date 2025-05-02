package users

import sttp.tapir.*
import sttp.tapir.json.circe.*
import api.UnathorizedError
import org.http4s.client.Client
import cats.effect.IO
import org.http4s.Uri
import cats.effect.syntax.all.*
import sttp.tapir.server.PartialServerEndpoint
import org.http4s.{Request, Method}
import org.http4s.Header
import org.typelevel.ci.CIString
import entities.{User, UserPayload}

class AuthenticationService(client: Client[IO], usersService: UsersService):
  extension [I, E >: UnathorizedError, O, R](securityEndpoint: Endpoint[String, I, E, O, R])
    def authenticate: PartialServerEndpoint[String, (User, String), I, E, O, R, IO] =
      securityEndpoint.serverSecurityLogic: authBearerToken =>
        val uri = Uri.unsafeFromString("https://api.github.com/user")

        client
          .expect[UserPayload](
            Request[IO](Method.GET, uri)
              .putHeaders(Header.Raw(CIString("Authorization"), s"Bearer $authBearerToken"))
          )
          .redeemWith(
            _ => IO.pure(Left(UnathorizedError("invalid user access token"))),
            userPayload => usersService.findOrCreateSingle(userPayload.toUser).map(Right(_, authBearerToken))
          )
