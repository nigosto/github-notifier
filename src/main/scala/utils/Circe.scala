package utils

import io.circe.Codec
import io.circe.Encoder
import io.circe.Decoder
import io.circe.derivation.Configuration
import io.circe.derivation.renaming.{snakeCase, pascalCase}

given inputDataConfiguration: Configuration = Configuration
  .default
  .withDiscriminator("type")
  .withTransformConstructorNames(_.toLowerCase)
  .withTransformMemberNames(snakeCase)

given outputDataConfiguration: Configuration = Configuration
  .default
  .withDiscriminator("type")
  .withTransformConstructorNames(s => s.charAt(0).toLower +: s.substring(1))

object CirceUtils:  
  val stringCodec = Codec.from(Decoder[String], Encoder[String])

  def unwrappedCodec[W, U : Encoder : Decoder](wrap: U => W)(unwrap: W => U): Codec[W] =
    Codec.from(Decoder[U], Encoder[U]).iemap(u => Right(wrap(u)))(unwrap)

  def dropNulls[A](encoder: Encoder[A]): Encoder[A] =
    encoder.mapJson(_.dropNullValues)