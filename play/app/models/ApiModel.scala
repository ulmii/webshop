package models


trait ApiModel[+Self <: ApiModel[Self]] {
  def id: Option[String]

  def _updated: Option[Long]

  def createNew(updated: Option[Long]): Self = {
    makeNew(updated)
  }

  def copyNew(updated: Option[Long]): Self = {
    makeNew(updated)
  }

  protected def makeNew(updated: Option[Long]): Self
}
