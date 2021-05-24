package repository

import java.time.Instant

import javax.inject.Inject
import models.{Basket, User}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.compat._
import reactivemongo.api.bson.{BSONDocument, BSONObjectID}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}

import scala.concurrent.{ExecutionContext, Future}

class BasketRepository @Inject()(
                                  implicit executionContext: ExecutionContext,
                                  reactiveMongoApi: ReactiveMongoApi,
                                  userRepository: UserRepository
                                ) {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("users"))

  def findAll(limit: Int = 100): Future[Seq[Basket]] = {
    collection.flatMap(
      _.find(BSONDocument(), Option.empty[User])
        .cursor[User](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[User]]())
        .map(s => s.filter(user => user.basket.isDefined).map(user => user.basket.get)))
  }


  def findUserBasket(username: String): Future[Basket] = {
    collection.flatMap(
      _.find(BSONDocument("username" -> username), Option.empty[User])
        .one[User]
        .map(s => s.filter(user => user.basket.isDefined)
          .map(user => user.basket.get))
    )
  }

  def update(username: String, apiModel: User): Future[WriteResult] = {

    collection.flatMap(
      _.update(ordered = false).one(BSONDocument("username" -> username),
        apiModel.copyNew(Some(Instant.now().getEpochSecond))))
  }

  //  def create(apiModel: Basket): Future[WriteResult] = {
  //
  //    userRepository.findByUsername(apiModel.username)
  //      .foreach(user => {
  //      val objectIdTryResult = BSONObjectID.parse(user.get._id.get)
  //      objectIdTryResult match {
  //        case Success(objectId) => userRepository.update(objectId, user.get.copyNew(Some(Instant.now().getEpochSecond)))
  //      }
  //    })
  //
  //    return Future.successful();
  //  }

  //
  //  override def update(id: BSONObjectID, apiModel: Basket): Future[WriteResult] = {
  //
  //    collection.flatMap(
  //      _.update(ordered = false).one(BSONDocument("_id" -> id),
  //        apiModel.copy(
  //          _updated = Some(Instant.now().getEpochSecond))))
  //    ) ) )
  //  }
  //
  def delete(id: BSONObjectID): Future[WriteResult] = {
    collection.flatMap(
      _.delete().one(BSONDocument("_id" -> id), Some(1))
    )
  }
}
