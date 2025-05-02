package events

import events.EventsEndpoints.receiveEventEndpoint
import sttp.tapir.*
import sttp.tapir.json.circe.*
import cats.effect.IO
import org.http4s.{Response, Status, Uri, Request, Method, Header}
import cats.syntax.all.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import io.circe.parser.*
import io.circe.syntax.*
import api.BadRequestError
import io.circe.Json
import emails.{EmailsService, EmailNotSendError}
import users.preferences.PreferencesService
import users.AuthenticationService

class EventsController(
  eventsService: EventsService,
  preferencesService: PreferencesService,
  webhookSecret: String
)(
  authenticationService: AuthenticationService
):
  import authenticationService.authenticate

  def receiveEvent = receiveEventEndpoint.serverLogic: (payload, secret, `type`) =>
    validateSignature(payload, secret).ifM(
      parsePayload(payload, `type`).fold(
        e => IO.println(e) >> IO.pure(Left(BadRequestError("Invalid event payload"))),
        event =>
          for
            users <- preferencesService.findUsersByPreferenceForRepository(event.preference, event.repository.id)
            _ <- eventsService.logAndNotify(event, users)
          yield Right(())
      ),
      IO.pure(Left(BadRequestError("Invalid signature")))
    )

  val endpoints = List(receiveEvent)

  // Wrapping the method in IO, because it contains side effects
  private def validateSignature(payload: String, signature: String): IO[Boolean] = IO:
    val mac = Mac.getInstance("HmacSHA256")
    val keySpec = new SecretKeySpec(webhookSecret.getBytes, "HmacSHA256")
    mac.init(keySpec)
    val computedHash = "sha256=" + Hex.encodeHexString(mac.doFinal(payload.getBytes))
    computedHash == signature

  private def typeToEvent(`type`: String) = "incoming" ++ `type`.split("_").reduce(_ ++ _).toLowerCase ++ "event"

  private def parsePayload(payload: String, `type`: String) =
    val eventType: Json = Map("type" -> typeToEvent(`type`)).asJson
    parse(payload).map(_.deepMerge(eventType)).flatMap(_.as[IncomingGithubEvent])
