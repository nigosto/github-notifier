package history

import api.AppEndpoints.*
import sttp.tapir.*
import sttp.tapir.json.circe.*
import entities.RepositoryId

object HistoryEndpoints:
  val historyBaseEndpoint = v1BaseEndpoint.in("history")

  val getPaginatedHistoryEndpoint =
    historyBaseEndpoint.secure
      .in(pagination.and(query[RepositoryId]("repositoryId")))
      .out(jsonBody[List[HistoryRecord]])
      .get

