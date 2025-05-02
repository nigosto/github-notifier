package history

import doobie.util.fragment.Fragment

trait Audit[A]:
  def projection: HistoryRecordProjection
  def table: String

object Audit:
  def apply[A](using a: Audit[A]): Audit[A] = a
