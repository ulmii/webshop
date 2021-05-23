package repository

import javax.inject.Inject
import models.Product
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.compat._

import scala.concurrent.{ExecutionContext, Future}

class ProductRepository @Inject()(
                                   implicit executionContext: ExecutionContext,
                                   reactiveMongoApi: ReactiveMongoApi
                                 )
  extends AbstractRepository[Product] {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("products"))
}
