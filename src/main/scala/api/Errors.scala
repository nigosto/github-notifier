package api

import io.circe.Codec
import sttp.tapir.Schema

sealed trait HttpError(message: String)
case class NotFoundError(message: String) extends HttpError(message) derives Codec, Schema
case class UnathorizedError(message: String) extends HttpError(message) derives Codec, Schema
case class BadRequestError(message: String) extends HttpError(message) derives Codec, Schema