package repository

import javax.inject.Inject
import models.User
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.compat._

import scala.concurrent.{ExecutionContext, Future}

class UserRepository @Inject()(
                                implicit executionContext: ExecutionContext,
                                reactiveMongoApi: ReactiveMongoApi
                              )
  extends AbstractRepository[User] {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("users"))

  def findByUsername(username: String): Future[Option[User]] = {
    collection.flatMap(
      _.find(BSONDocument("username" -> username), Option.empty[User])
        .one[User]
    )
  }
}
