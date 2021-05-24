package models

import reactivemongo.bson.BSONObjectID

trait ApiModel[+Self <: ApiModel[Self]] {
  def _id: Option[BSONObjectID]

  def _updated: Option[Long]

  def copyNew(updated: Option[Long]): Self = {
    makeNew(updated)
  }

  protected def makeNew(updated: Option[Long]): Self
}
