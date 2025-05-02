package api

import sttp.tapir.*
import sttp.tapir.json.circe.*
import io.circe.Codec
import sttp.model.StatusCode.Unauthorized

object AppEndpoints:
  val baseEndpoint: PublicEndpoint[Unit, Unit, Unit, Any] = endpoint
  val v1BaseEndpoint: Endpoint[Unit, Unit, Unit, Unit, Any] = baseEndpoint.in("v1")

  val pagination = query[Int]("page").and(query[Int]("size"))

  extension [I, O, R](endpoint: PublicEndpoint[I, Unit, O, R])
    def secure: Endpoint[String, I, UnathorizedError, O, R] =
      endpoint
        .securityIn(auth.bearer[String]())
        .errorOut(statusCode(Unauthorized).and(jsonBody[UnathorizedError].description("unauthorized access")))