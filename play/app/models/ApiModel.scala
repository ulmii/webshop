package models

trait ApiModel[+Self <: ApiModel[Self]] {
  def _id: Option[String]

  def _updateTime: Option[Long]

  def copyNew(updateTime: Option[Long]): Self = {
    makeNew(updateTime)
  }

  protected def makeNew(updateTime: Option[Long]): Self
}
