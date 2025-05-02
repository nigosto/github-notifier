package events

import sttp.model.StatusCode.{InternalServerError, BadRequest, Unauthorized}
import api.AppEndpoints.*
import sttp.tapir.*
import sttp.tapir.json.circe.*
import api.BadRequestError

object EventsEndpoints:
  val eventsBaseEndpoint = v1BaseEndpoint.in("events")

  val receiveEventEndpoint =
    eventsBaseEndpoint
      .in(
        stringBody
          .and(header[String]("X-Hub-Signature-256"))
          .and(header[String]("X-GitHub-Event"))
      )
      .errorOut(statusCode(BadRequest).and(jsonBody[BadRequestError]))
      .post
