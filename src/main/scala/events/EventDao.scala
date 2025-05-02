package events

import database.DatabaseTransactor
import doobie.*
import doobie.implicits.*
import doobie.postgres.sqlstate
import cats.syntax.all.*
import cats.effect.IO
import doobie.postgres.implicits.JavaTimeLocalDateTimeMeta
import entities.RepositoryId
import history.HistoryRecord
import history.Audit

class EventDao(transactor: DatabaseTransactor):
  def create(event: GithubEvent) =
    event.insertQuery.update.run.as(event).transact(transactor)

  // TODO: use reflection to reduce repetition
  def findPaginated(limit: Int, offset: Int, repositoryId: RepositoryId) =
    (
      fr"(" ++
        selectEventQuery[PullRequestEvent](repositoryId) ++ fr"UNION ALL" ++
        selectEventQuery[PushEvent](repositoryId) ++ fr"UNION ALL" ++
        selectEventQuery[IssueEvent](repositoryId) ++ fr"UNION ALL" ++
        selectEventQuery[CreateEvent](repositoryId) ++ fr"""
      )
      ORDER BY createdAt DESC
      LIMIT $limit
      OFFSET $offset;
      """
    ).query[HistoryRecord].to[List].transact(transactor)

  def deleteAll =
    sql"""
      DELETE FROM "github-notifier".pull_request_events;
      DELETE FROM "github-notifier".push_events;
      DELETE FROM "github-notifier".issue_events;
      DELETE FROM "github-notifier".create_events;
    """.update.run
      .transact(transactor)

  private def selectEventQuery[A : Audit](repositoryId: RepositoryId) =
    fr"SELECT" ++
      Audit[A].projection.toFragment ++
      Fragment.const(s" FROM \"github-notifier\".${Audit[A].table}") ++
      fr""" WHERE repositoryId = ${repositoryId.id}"""
