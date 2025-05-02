package entities

import doobie.util.Read
import io.circe.Codec
import sttp.tapir.{Schema, SchemaType}
import utils.CirceUtils

case class UserId(id: Int) extends AnyVal

object UserId:
  given Read[UserId] = Read[Int].map(UserId.apply)
  given Codec[UserId] = CirceUtils.unwrappedCodec(UserId.apply)(_.id)
  given Schema[UserId] = Schema(SchemaType.SInteger())

case class PullRequestId(id: Int) extends AnyVal

object PullRequestId:
  given Read[PullRequestId] = Read[Int].map(PullRequestId.apply)
  given Read[Option[PullRequestId]] = Read[Option[Int]].map(_.map(PullRequestId.apply))
  given Codec[PullRequestId] = CirceUtils.unwrappedCodec(PullRequestId.apply)(_.id)
  given Schema[PullRequestId] = Schema(SchemaType.SInteger())

case class RepositoryId(id: Int) extends AnyVal

object RepositoryId:
  given Read[RepositoryId] = Read[Int].map(RepositoryId.apply)
  given Codec[RepositoryId] = CirceUtils.unwrappedCodec(RepositoryId.apply)(_.id)
  given Schema[RepositoryId] = Schema(SchemaType.SInteger())

case class IssueId(id: Long) extends AnyVal

object IssueId:
  given Read[IssueId] = Read[Long].map(IssueId.apply)
  given Read[Option[IssueId]] = Read[Option[Long]].map(_.map(IssueId.apply))
  given Codec[IssueId] = CirceUtils.unwrappedCodec(IssueId.apply)(_.id)
  given Schema[IssueId] = Schema(SchemaType.SInteger())

