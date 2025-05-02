package permissions

import users.UsersService
import repositories.RepositoriesService
import org.http4s.client.Client
import cats.effect.IO
import cats.effect.kernel.Resource

case class PermissionsModule(permissionsService: PermissionsService)

object PermissionsModule:
  def apply(usersService: UsersService, repositoriesService: RepositoriesService, client: Client[IO])
    : Resource[IO, PermissionsModule] =
    val permissionsService = new PermissionsService(usersService, repositoriesService, client)

    Resource.pure(new PermissionsModule(permissionsService))
