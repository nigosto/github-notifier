import cats.effect.IOApp
import cats.effect.kernel.Resource
import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder
import cats.effect.IO
import com.comcast.ip4s.ipv4
import com.comcast.ip4s.port
import com.typesafe.config.ConfigFactory
import cats.effect.ExitCode
import config.AppConfig
import database.DatabaseModule
import events.EventsModule
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import users.UsersModule
import repositories.RepositoriesModule
import pullRequests.PullRequstsModule
import org.http4s.ember.client.EmberClientBuilder
import emails.EmailsModule
import issues.IssuesModule
import permissions.PermissionsModule
import history.HistoryModule

object GitHubHistoryApp extends IOApp.Simple:
  val app: Resource[IO, Server] = for
    config <- Resource
      .eval(IO.blocking(ConfigFactory.load()))
      .map(_.getConfig("app"))
      .map(AppConfig.fromConfig)

    client <- EmberClientBuilder.default[IO].build

    databaseModule <- DatabaseModule(config.database)
    usersModule <- UsersModule(config.clientId, databaseModule.transactor, client)
    emailsModule <- EmailsModule(config.smtp)
    repositoriesModule <- RepositoriesModule(databaseModule.transactor, usersModule.usersService)

    issuesModule <- IssuesModule(
      databaseModule.transactor,
      usersModule.usersService,
      repositoriesModule.repositoriesService
    )

    pullRequestsModule <- PullRequstsModule(
      databaseModule.transactor,
      usersModule.usersService,
      repositoriesModule.repositoriesService
    )

    permissionsModule <- PermissionsModule(
      usersModule.usersService,
      repositoriesModule.repositoriesService,
      client
    )

    eventsModule <- EventsModule(
      config.webhookSecret,
      databaseModule.transactor,
      usersModule.usersService,
      usersModule.preferencesService,
      repositoriesModule.repositoriesService,
      pullRequestsModule.pullRequestsService,
      issuesModule.issuesService,
      emailsModule.emailsService,
      usersModule.authenticationService
    )

    historyModule <- HistoryModule(
      eventsModule.eventDao,
      permissionsModule.permissionsService,
      usersModule.authenticationService
    )

    apiEndpoints = eventsModule.endpoints ::: usersModule.endpoints ::: historyModule.endpoints
    docEndpoints = SwaggerInterpreter().fromServerEndpoints[IO](apiEndpoints, "github-notifier-app", "1.0.0")

    routes = Http4sServerInterpreter[IO]().toRoutes(apiEndpoints ::: docEndpoints).orNotFound

    server <- EmberServerBuilder
      .default[IO]
      .withHost(config.http.host)
      .withPort(config.http.port)
      .withHttpApp(routes)
      .build
  yield server

  def run: IO[Unit] =
    app
      .use(httpServer => IO.never)
      .onCancel(IO.println("Stopping server..."))
      .as(ExitCode.Success)
