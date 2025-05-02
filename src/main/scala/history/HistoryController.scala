package history

import entities.{RepositoryId, UserId}
import cats.data.OptionT
import repositories.RepositoriesService
import users.UsersService
import org.http4s.{Uri, Request, Method, Header}
import users.AuthenticationService
import history.HistoryEndpoints.getPaginatedHistoryEndpoint
import cats.effect.IO
import api.UnathorizedError
import org.http4s.client.Client
import org.typelevel.ci.CIString
import permissions.PermissionsService

class HistoryController(
  historyService: HistoryService,
  permissionsService: PermissionsService
)(
  authenticationService: AuthenticationService
):
  import authenticationService.authenticate

  def getPaginatedHistory = getPaginatedHistoryEndpoint.authenticate.serverLogic: (user, authBearerToken) =>
    (page, size, repositoryId) =>
      permissionsService
        .checkUserPermissionsForRepository(repositoryId, user.id, authBearerToken)
        .ifM(
          historyService.getPaginatedHistoryRecords(page, size, repositoryId).map(Right(_)),
          IO.pure(Left(UnathorizedError("user does not have access to this repository")))
        )

  val endpoints = List(getPaginatedHistory)
