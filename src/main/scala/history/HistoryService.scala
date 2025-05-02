package history

import events.EventDao
import entities.RepositoryId
import cats.effect.IO

class HistoryService(eventDao: EventDao):
  def getPaginatedHistoryRecords(page: Int, size: Int, repositoryId: RepositoryId): IO[List[HistoryRecord]] =
    if page < 1 || size < 1 then IO.pure(List.empty)
    else eventDao.findPaginated(size, (page - 1) * size, repositoryId)
