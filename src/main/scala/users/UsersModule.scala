package users

import database.DatabaseTransactor
import cats.effect.kernel.Resource
import cats.effect.IO
import sttp.tapir.server.ServerEndpoint
import sttp.capabilities.fs2.Fs2Streams
import org.http4s.client.Client
import users.preferences.PreferencesService
import users.preferences.PreferencesDao

case class UsersModule(
  usersService: UsersService,
  preferencesService: PreferencesService,
  authenticationService: AuthenticationService,
  endpoints: List[ServerEndpoint[Fs2Streams[IO], IO]]
)

object UsersModule:
  def apply(clientId: String, transactor: DatabaseTransactor, client: Client[IO]): Resource[IO, UsersModule] =
    val userDao = new UserDao(transactor)
    val preferencesDao = new PreferencesDao(transactor)

    val usersService = new UsersService(userDao)
    val authenticationService = new AuthenticationService(client, usersService)
    val preferencesService = new PreferencesService(preferencesDao)
    val usersController = new UsersController(client, clientId, usersService, preferencesService)(authenticationService)

    Resource.pure(new UsersModule(usersService, preferencesService, authenticationService, usersController.endpoints))
