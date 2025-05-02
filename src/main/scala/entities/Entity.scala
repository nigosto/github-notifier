package entities

import io.circe.Codec
import sttp.tapir.Schema
import doobie.util.fragment.Fragment
import doobie.*
import doobie.implicits.*

trait Entity:
  def rawId: Long
  def insertValues: Fragment

case class User(
  id: UserId,
  username: String,
  email: Option[String],
  avatarUrl: String
) extends Entity derives Codec, Schema:
  def rawId: Long = id.id
  def insertValues = fr"""VALUES ($rawId, $username, $email, $avatarUrl)"""

case class Repository(
  id: RepositoryId,
  name: String,
  ownerId: UserId,
  description: String,
) extends Entity derives Codec, Schema:
  def rawId: Long = id.id
  def insertValues = fr"""VALUES ($rawId, $name, ${ownerId.id}, $description)"""

case class PullRequest(
  id: PullRequestId,
  title: String,
  body: String,
  repositoryId: RepositoryId,
  authorId: UserId
) extends Entity derives Codec, Schema:
  def rawId: Long = id.id
  def insertValues = fr"""VALUES ($rawId, $title, $body, ${repositoryId.id}, ${authorId.id})"""
  
case class Issue (
  id: IssueId,
  title: String,
  body: String,
  repositoryId: RepositoryId,
  authorId: UserId
) extends Entity derives Codec, Schema:
  def rawId: Long = id.id
  def insertValues = fr"""VALUES ($rawId, $title, $body, ${repositoryId.id}, ${authorId.id})"""
