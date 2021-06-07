package repository

import java.time.Instant

import models.ApiModel
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.compat._
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}

import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractRepository[T <: ApiModel[T] : BSONDocumentReader : BSONDocumentWriter](
                                                                                               implicit executionContext: ExecutionContext,
                                                                                               reactiveMongoApi: ReactiveMongoApi
                                                                                             )
  extends Repository[T] {
  def collection: Future[BSONCollection]

  def findAll(limit: Int = 100): Future[Seq[T]] = {

    collection.flatMap(
      _.find(BSONDocument(), Option.empty[T])
        .cursor[T](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[T]]())
    )
  }

  def findOne(id: String): Future[Option[T]] = {
    collection.flatMap(_.find(BSONDocument("id" -> id), Option.empty[T]).one[T])
  }

  def create(apiModel: T): Future[WriteResult] = {
    collection.flatMap(_.insert(ordered = false)
      .one(apiModel.copyNew(Some(Instant.now().getEpochSecond))))
  }

  def createAndGet(apiModel: T): Future[T] = {
    collection.flatMap(_.insert(ordered = false)
      .one(apiModel.copyNew(Some(Instant.now().getEpochSecond))))

      Future.successful(apiModel)
  }

  def update(id: String, apiModel: T): Future[WriteResult] = {

    collection.flatMap(
      _.update(ordered = false).one(BSONDocument("id" -> id),
        apiModel.copyNew(Some(Instant.now().getEpochSecond))))
  }

  def delete(id: String): Future[WriteResult] = {
    collection.flatMap(
      _.delete().one(BSONDocument("id" -> id), Some(1))
    )
  }
}
