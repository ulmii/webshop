package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

case class Basket(
                   username: String,
                   products: Option[List[Product]],
                   _updated: Option[Long]
                 )

object Basket {
  implicit val fmt: OFormat[Basket] = Json.format[Basket]

  implicit object BasketBSONReader extends BSONDocumentReader[Basket] {
    def read(doc: BSONDocument): Basket = Basket(
      doc.getAs[String]("username").get,
      doc.getAs[List[Product]]("products"),
      doc.getAs[Long]("_updated")
    )
  }

  implicit object BasketBSONWriter extends BSONDocumentWriter[Basket] {
    def write(basket: Basket): BSONDocument = BSONDocument(
      "username" -> basket.username,
      "products" -> basket.products,
      "_updated" -> basket._updated
    )
  }

}




