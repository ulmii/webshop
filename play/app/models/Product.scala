package models


import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.{BSONObjectID, _}

case class Product(
                    _id: Option[String],
                    title: String,
                    description: String,
                    _creationTime: Option[Long],
                    _updateTime: Option[Long]
                  )

object Product {
  implicit val bObjectIdFormat: OFormat[BSONObjectID] = Json.format[BSONObjectID]
  implicit val fmt: OFormat[Product] = Json.format[Product]

  implicit object ProductBSONReader extends BSONDocumentReader[Product] {
    def read(doc: BSONDocument): Product = {
      Product(
        doc.getAs[BSONObjectID]("_id").map(dt => dt.stringify),
        doc.getAs[String]("title").get,
        doc.getAs[String]("description").get,
        doc.getAs[Long]("_creationTime"),
        doc.getAs[Long]("_updateTime")
      )
    }
  }

  implicit object ProductBSONWriter extends BSONDocumentWriter[Product] {
    def write(product: Product): BSONDocument = {
      BSONDocument(
        "_id" -> product._id,
        "title" -> product.title,
        "description" -> product.description,
        "_creationTime" -> product._creationTime,
        "_updateTime" -> product._updateTime
      )
    }
  }

}
