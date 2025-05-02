package permissions

import users.UsersService
import repositories.RepositoriesService
import org.http4s.client.Client
import cats.effect.IO
import entities.{RepositoryId, UserId}
import cats.data.OptionT
import org.http4s.{Uri, Request, Method, Header}
import org.typelevel.ci.CIString

class PermissionsService(usersService: UsersService, repositoriesService: RepositoriesService, client: Client[IO]):
  def checkUserPermissionsForRepository(repositoryId: RepositoryId, userId: UserId, authBearerToken: String) =
    val basePath = "https://api.github.com/repos"

    (for
      repository <- OptionT(repositoriesService.find(repositoryId))
      owner <- OptionT(usersService.find(repository.ownerId))
      user <- OptionT(usersService.find(userId))
      uri = Uri.unsafeFromString(
        s"$basePath/${owner.username}/${repository.name}/collaborators/${user.username}"
      )
      response <- OptionT(
        client
          .expect[Unit](
            Request[IO](Method.GET, uri)
              .putHeaders(Header.Raw(CIString("Authorization"), s"Bearer $authBearerToken"))
          )
          .redeem(_ => None, Some(_))
      )
    yield response).isDefined
