package repository

import java.time.Instant

import javax.inject.Inject
import models.User
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.compat._
import reactivemongo.api.bson.{BSONDocument, BSONObjectID}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}

import scala.concurrent.{ExecutionContext, Future}

class UserRepository @Inject()(
                                implicit executionContext: ExecutionContext,
                                reactiveMongoApi: ReactiveMongoApi
                              ) {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("users"))

  def findAll(limit: Int = 100): Future[Seq[User]] = {

    collection.flatMap(
      _.find(BSONDocument(), Option.empty[User])
        .cursor[User](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[User]]())
    )
  }

  def findOne(id: BSONObjectID): Future[Option[User]] = {
    collection.flatMap(_.find(BSONDocument("_id" -> id), Option.empty[User]).one[User])
  }

  def create(User: User): Future[WriteResult] = {
    collection.flatMap(_.insert(ordered = false)
      .one(User.copy(_updateTime = Some(Instant.now().getEpochSecond))))
  }

  def update(id: BSONObjectID, User: User): Future[WriteResult] = {

    collection.flatMap(
      _.update(ordered = false).one(BSONDocument("_id" -> id),
        User.copy(
          _updateTime = Some(Instant.now().getEpochSecond))))
  }

  def delete(id: BSONObjectID): Future[WriteResult] = {
    collection.flatMap(
      _.delete().one(BSONDocument("_id" -> id), Some(1))
    )
  }
}
