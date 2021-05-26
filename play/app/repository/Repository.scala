package repository

import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

trait Repository[T] {
  def findAll(limit: Int = 100): Future[Seq[T]]

  def findOne(id: BSONObjectID): Future[Option[T]]

  def create(apiModel: T): Future[WriteResult]

  def update(id: BSONObjectID, apiModel: T): Future[WriteResult]

  def delete(id: BSONObjectID): Future[WriteResult]
}
