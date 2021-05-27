package repository

import java.time.Instant

import javax.inject.Inject
import models.{User, UserDetails}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.compat._
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}

import scala.concurrent.{ExecutionContext, Future}

class UserDetailsRepository @Inject()(
                                       implicit executionContext: ExecutionContext,
                                       reactiveMongoApi: ReactiveMongoApi,
                                       userRepository: UserRepository,
                                       productRepository: ProductRepository
                                     )
  extends Repository[UserDetails] {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("users"))

  override def findAll(limit: Int = 100): Future[Seq[UserDetails]] = {
    collection.flatMap(
      _.find(BSONDocument(), Option.empty[User])
        .cursor[User](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[User]]())
        .map(s => s.filter(user => user.details.isDefined).map(user => user.details.get)))
  }

  override def findOne(id: String): Future[Option[UserDetails]] = {
    collection.flatMap(
      _.find(BSONDocument("id" -> id), Option.empty[User])
        .one[User]
        .map(s => s.filter(user => user.details.isDefined)
          .map(user => user.details.get))
    )
  }

  def update(id: String, apiModel: UserDetails): Future[WriteResult] = {

    collection.flatMap(col => {
      val updateBuilder = col.update(true)
      updateBuilder.one(BSONDocument("id" -> id),
        BSONDocument("$set" -> BSONDocument(
          "details" -> apiModel.copy(_updated = Some(Instant.now().getEpochSecond))
        )))
    })
  }

  def delete(id: String): Future[WriteResult] = {
    collection.flatMap(
      _.delete().one(BSONDocument("id" -> id), Some(1))
    )
  }

  override def create(apiModel: UserDetails): Future[WriteResult] = ???
}
