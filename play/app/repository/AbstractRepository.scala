package repository

import java.time.Instant

import models.ApiModel
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.compat._
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}

import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractRepository[T <: ApiModel[T] : BSONDocumentReader : BSONDocumentWriter](
                                                                                               implicit executionContext: ExecutionContext,
                                                                                               reactiveMongoApi: ReactiveMongoApi
                                                                                             ) {
  def collection: Future[BSONCollection]

  def findAll(limit: Int = 100): Future[Seq[T]] = {

    collection.flatMap(
      _.find(BSONDocument(), Option.empty[T])
        .cursor[T](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[T]]())
    )
  }

  def findOne(id: BSONObjectID): Future[Option[T]] = {
    collection.flatMap(_.find(BSONDocument("_id" -> id), Option.empty[T]).one[T])
  }

  def create(apiModel: T): Future[WriteResult] = {
    collection.flatMap(_.insert(ordered = false)
      .one(apiModel.copyNew(Some(Instant.now().getEpochSecond))))
  }

  def update(id: BSONObjectID, apiModel: T): Future[WriteResult] = {

    collection.flatMap(
      _.update(ordered = false).one(BSONDocument("_id" -> id),
        apiModel.copyNew(Some(Instant.now().getEpochSecond))))
  }

  def delete(id: BSONObjectID): Future[WriteResult] = {
    collection.flatMap(
      _.delete().one(BSONDocument("_id" -> id), Some(1))
    )
  }
}
