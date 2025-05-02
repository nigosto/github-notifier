package users.preferences

import database.DatabaseTransactor
import entities.{UserId, User, RepositoryId}
import doobie.*
import doobie.implicits.*
import doobie.postgres.sqlstate
import cats.syntax.functor.*
import cats.effect.IO

class PreferencesDao(transactor: DatabaseTransactor):
  def create(userId: UserId, repositoryId: RepositoryId, preferences: List[Preference]) =
    val insertPreferencesQuery = """
        INSERT INTO "github-notifier".preferences (userId, repositoryId, preference)
        VALUES (?, ?, ?)
      """

    Update[(Int, Int, String)](insertPreferencesQuery)
      .updateMany(preferences.map(preference => (userId.id, repositoryId.id, preference.toString)))
      .as(preferences)
      .transact(transactor)

  def findPreferencesForUserByRepositoryId(userId: UserId, repositoryId: RepositoryId) = 
    sql"""
      SELECT preference
      FROM "github-notifier".preferences
      WHERE userId = ${userId.id}
      AND repositoryId = ${repositoryId.id}
    """.query[Preference].to[List].transact(transactor)

  def findUsersByPreferenceForRepository(preference: Preference, repositoryId: RepositoryId) = 
    sql"""
      SELECT DISTINCT userId, username, email, avatarUrl
      FROM "github-notifier".preferences
      JOIN "github-notifier".users ON "github-notifier".users.id = userId
      WHERE repositoryId = ${repositoryId.id}
      AND preference = ${preference.toString}
    """.query[User].to[List].transact(transactor)

  def delete(userId: UserId, repositoryId: RepositoryId, preferences: List[Preference]) =
    val deletePreferencesQuery = s"""
      DELETE FROM "github-notifier".preferences
      WHERE userId = ${userId.id} 
      AND repositoryId = ${repositoryId.id}
      AND preference = ?
    """

    Update[String](deletePreferencesQuery)
      .updateMany(preferences.map(_.toString))
      .as(preferences)
      .transact(transactor)

  def deleteAll = sql"""
    DELETE FROM "github-notifier".preferences
  """
  .update
  .run
  .transact(transactor)
