package entities

import io.circe.Codec
import sttp.tapir.Schema
import org.http4s.EntityDecoder
import cats.effect.IO
import org.http4s.circe.*

case class UserPayload(
  id: UserId,
  login: String,
  email: Option[String],
  avatar_url: String
) derives Codec,
      Schema:
  def toUser = User(id, login, email, avatar_url)

object UserPayload:
  given EntityDecoder[IO, UserPayload] = jsonOf

case class RepositoryPayload(
  id: RepositoryId,
  name: String,
  owner: UserPayload,
  description: Option[String]
) derives Codec,
      Schema:
  def toRepository = Repository(id, name, owner.id, description.getOrElse(""))

case class PullRequestPayload(
  id: PullRequestId,
  title: String,
  body: Option[String],
  base: Base,
  user: UserPayload
) derives Codec,
      Schema:
  def toPullRequest = PullRequest(id, title, body.getOrElse(""), base.repo.id, user.id)

case class Base(repo: RepositoryPayload) derives Codec, Schema

case class IssuePayload(
  id: IssueId,
  title: String,
  body: Option[String],
  user: UserPayload
) derives Codec,
      Schema:
  def toIssue(repositoryId: RepositoryId): Issue = Issue(id, title, body.getOrElse(""), repositoryId, user.id)
