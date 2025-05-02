package events

import sttp.tapir.server.ServerEndpoint
import cats.effect.IO
import cats.effect.kernel.Resource
import users.UsersService
import repositories.RepositoriesService
import pullRequests.PullRequestsService
import database.DatabaseTransactor
import emails.EmailsService
import users.preferences.PreferencesService
import users.AuthenticationService
import org.http4s.client.Client
import issues.IssuesService

case class EventsModule(eventDao: EventDao, endpoints: List[ServerEndpoint[Any, IO]])

object EventsModule:
  def apply(
    webhookSecret: String,
    transactor: DatabaseTransactor,
    usersService: UsersService,
    preferencesService: PreferencesService,
    repositoriesService: RepositoriesService,
    pullRequestsService: PullRequestsService,
    issuesService: IssuesService,
    emailsService: EmailsService,
    authenticationService: AuthenticationService
  ): Resource[IO, EventsModule] =
    val eventDao = new EventDao(transactor)
    val eventsService = new EventsService(
      eventDao,
      usersService,
      repositoriesService,
      pullRequestsService,
      issuesService,
      emailsService
    )
    val eventsController =
      new EventsController(eventsService, preferencesService, webhookSecret)(authenticationService)

    Resource.pure(new EventsModule(eventDao, eventsController.endpoints))
