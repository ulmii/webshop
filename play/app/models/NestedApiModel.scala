package models

trait NestedApiModel[+Self <: NestedApiModel[Self]] {
  def _updated: Option[Long]

  def copyNew(updated: Option[Long]): Self = {
    makeNew(updated)
  }

  protected def makeNew(updated: Option[Long]): Self
}
