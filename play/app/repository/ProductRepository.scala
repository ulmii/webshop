package repository

import java.time.Instant

import javax.inject.Inject
import models.Product
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.compat._
import reactivemongo.api.bson.{BSONDocument, BSONObjectID}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}

import scala.concurrent.{ExecutionContext, Future}

class ProductRepository @Inject()(
                                   implicit executionContext: ExecutionContext,
                                   reactiveMongoApi: ReactiveMongoApi
                                 ) {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("products"))

  def findAll(limit: Int = 100): Future[Seq[Product]] = {

    collection.flatMap(
      _.find(BSONDocument(), Option.empty[Product])
        .cursor[Product](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[Product]]())
    )
  }

  def findOne(id: BSONObjectID): Future[Option[Product]] = {
    collection.flatMap(_.find(BSONDocument("_id" -> id), Option.empty[Product]).one[Product])
  }

  def create(Product: Product): Future[WriteResult] = {
    collection.flatMap(_.insert(ordered = false)
      .one(Product.copy(_creationTime = Some(Instant.now().getEpochSecond), _updateTime = Some(Instant.now().getEpochSecond))))
  }

  def update(id: BSONObjectID, Product: Product): Future[WriteResult] = {

    collection.flatMap(
      _.update(ordered = false).one(BSONDocument("_id" -> id),
        Product.copy(
          _updateTime = Some(Instant.now().getEpochSecond))))
  }

  def delete(id: BSONObjectID): Future[WriteResult] = {
    collection.flatMap(
      _.delete().one(BSONDocument("_id" -> id), Some(1))
    )
  }
}
