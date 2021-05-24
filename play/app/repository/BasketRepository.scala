package repository

import java.time.Instant

import javax.inject.Inject
import models.{Basket, User}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.compat._
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.{ExecutionContext, Future}

class BasketRepository @Inject()(
                                  implicit executionContext: ExecutionContext,
                                  reactiveMongoApi: ReactiveMongoApi,
                                  userRepository: UserRepository,
                                  productRepository: ProductRepository
                                ) {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("users"))

  def findAll(limit: Int = 100): Future[Seq[Basket]] = {
    collection.flatMap(
      _.find(BSONDocument(), Option.empty[User])
        .cursor[User](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[User]]())
        .map(s => s.filter(user => user.basket.isDefined).map(user => user.basket.get)))
  }

  def findUserBasket(id: BSONObjectID): Future[Option[Basket]] = {
    collection.flatMap(
      _.find(BSONDocument("_id" -> id), Option.empty[User])
        .one[User]
        .map(s => s.filter(user => user.basket.isDefined)
          .map(user => user.basket.get))
    )
  }

  def updateUserBasket(id: BSONObjectID, apiModel: Basket): Future[WriteResult] = {

    collection.flatMap(col => {
      val updateBuilder = col.update(true)
      updateBuilder.one(BSONDocument("_id" -> id),
        BSONDocument("$set" -> BSONDocument(
          "basket" -> apiModel.copy(_updated = Some(Instant.now().getEpochSecond))
        )))
    })
  }

  def delete(id: BSONObjectID): Future[WriteResult] = {
    collection.flatMap(
      _.delete().one(BSONDocument("_id" -> id), Some(1))
    )
  }
}
