package history

import sttp.tapir.server.ServerEndpoint
import cats.effect.IO
import events.EventDao
import users.{AuthenticationService, UsersService}
import repositories.RepositoriesService
import org.http4s.client.Client
import cats.effect.kernel.Resource
import permissions.PermissionsService

case class HistoryModule(endpoints: List[ServerEndpoint[Any, IO]])

object HistoryModule:
  def apply(eventDao: EventDao, permissionsService: PermissionsService, authenticationService: AuthenticationService)
    : Resource[IO, HistoryModule] =
    val historyService = new HistoryService(eventDao)
    val historyController =
      new HistoryController(historyService, permissionsService)(
        authenticationService
      )

    Resource.pure(new HistoryModule(historyController.endpoints))
