package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

case class Basket(
                   userId: String,
                   products: Option[Seq[Product]],
                   _updated: Option[Long]
                 )

object Basket {
  implicit val fmt: OFormat[Basket] = Json.format[Basket]

  implicit object BasketBSONReader extends BSONDocumentReader[Basket] {
    def read(doc: BSONDocument): Basket = Basket(
      doc.getAs[String]("userId").get,
      doc.getAs[Seq[Product]]("products"),
      doc.getAs[Long]("_updated")
    )
  }

  implicit object BasketBSONWriter extends BSONDocumentWriter[Basket] {
    def write(basket: Basket): BSONDocument = BSONDocument(
      "userId" -> basket.userId,
      "products" -> basket.products,
      "_updated" -> basket._updated
    )
  }

}




