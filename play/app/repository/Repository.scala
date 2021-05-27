package repository

import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait Repository[T] {
  def findAll(limit: Int = 100): Future[Seq[T]]

  def findOne(id: String): Future[Option[T]]

  def create(apiModel: T): Future[WriteResult]

  def update(id: String, apiModel: T): Future[WriteResult]

  def delete(id: String): Future[WriteResult]
}
