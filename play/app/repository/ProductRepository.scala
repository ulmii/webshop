package repository

import javax.inject.Inject
import models.Product
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{Cursor, ReadPreference}

import scala.concurrent.{ExecutionContext, Future}

class ProductRepository @Inject()(
                                   implicit executionContext: ExecutionContext,
                                   reactiveMongoApi: ReactiveMongoApi
                                 )
  extends AbstractRepository[Product] {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("products"))


  def getProductsByCategory(name: String, limit: Int = 100): Future[Seq[Product]] = {

    collection.flatMap(
      _.find(BSONDocument(), Option.empty[Product])
        .cursor[Product](ReadPreference.Primary)
        .collect[Seq](limit, Cursor.FailOnError[Seq[Product]]())
        .map(s => s.filter(product => product.category.name.equalsIgnoreCase(name)))
    )
  }
}
