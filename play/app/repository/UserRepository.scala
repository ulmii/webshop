package repository

import com.mohiva.play.silhouette.api.LoginInfo
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

  def findUser(loginInfo: LoginInfo): Future[Option[User]] = {
    collection.flatMap(_.find(BSONDocument("email" -> loginInfo.providerKey), Option.empty[User]).one[User])
  }
}
