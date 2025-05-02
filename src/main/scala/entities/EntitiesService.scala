package entities

import cats.effect.IO
import utils.AlternativeOps.{given}
import utils.AlternativeOps.<|>

class InvalidEntityError(message: String) extends Throwable(message)

trait EntitiesService[E <: Entity](entityDao: EntityDao[E]):
  def findOrCreateSingle(entity: E): IO[E] =
    (entityDao.create(entity) <|> entityDao.find(entity.rawId).map(_.get))
      .handleErrorWith(e => IO.raiseError(InvalidEntityError(e.getMessage)))
