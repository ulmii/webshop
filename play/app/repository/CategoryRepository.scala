package repository

import javax.inject.Inject
import models.Category
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.{BSONDocument, BSONObjectID}
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.compat._

import scala.concurrent.{ExecutionContext, Future}

class CategoryRepository @Inject()(
                                    implicit executionContext: ExecutionContext,
                                    reactiveMongoApi: ReactiveMongoApi
                                  )
  extends AbstractRepository[Category] {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("categories"))

  def findByName(name: String): Future[Option[Category]] = {
    collection.flatMap(_.find(BSONDocument("name" -> name), Option.empty[Category]).one[Category])
  }
}
