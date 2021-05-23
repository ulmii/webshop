package models

trait ApiModel[+Self <: ApiModel[Self]] {
  def _id: Option[String]

  def _updated: Option[Long]

  def copyNew(updated: Option[Long]): Self = {
    makeNew(updated)
  }

  protected def makeNew(updated: Option[Long]): Self
}
