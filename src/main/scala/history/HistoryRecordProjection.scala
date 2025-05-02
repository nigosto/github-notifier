package history

import doobie.util.fragment.Fragment

case class HistoryRecordProjection(fields: List[String]):
  def toFragment = Fragment.const(fields.mkString(", "))

object HistoryRecordProjection:
  def apply(
    `type`: String,
    action: Option[String],
    ref: Option[String],
    refType: Option[String],
    previousRef: Option[String],
    pullRequestId: Option[String],
    issueId: Option[String],
    repositoryId: Option[String],
    senderId: Option[String],
    createdAt: Option[String]
  ) = new HistoryRecordProjection(List(
    "id",
    s"'${`type`}'",
    action.getOrElse("NULL") ++ " as action",
    ref.getOrElse("NULL") ++ " as ref",
    refType.getOrElse("NULL") ++ " as refType",
    previousRef.getOrElse("NULL") ++ " as previousRef",
    pullRequestId.getOrElse("NULL") ++ " as pullRequestId",
    issueId.getOrElse("CAST(NULL AS INT)") ++ " as issueId",
    repositoryId.getOrElse("NULL") ++ " as repositoryId",
    senderId.getOrElse("NULL") ++ " as senderId",
    createdAt.getOrElse("NULL") ++ " as createdAt",
  ))
