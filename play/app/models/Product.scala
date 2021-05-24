package models


import format.BSONObjectIDFormat
import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.{BSONObjectID, _}

case class Product(
                    _id: Option[BSONObjectID] = Some(BSONObjectID.generate()),
                    title: Option[String],
                    description: Option[String],
                    _updated: Option[Long]
                  )
  extends ApiModel[Product] {
  override protected def makeNew(updated: Option[Long]): Product = new Product(_id, title, description, updated)
}

object Product {
  implicit val objectIdFormat: BSONObjectIDFormat.type = BSONObjectIDFormat
  implicit val fmt: OFormat[Product] = Json.format[Product]

  implicit object ProductBSONReader extends BSONDocumentReader[Product] {
    def read(doc: BSONDocument): Product = Product(
      doc.getAs[BSONObjectID]("_id"),
      doc.getAs[String]("title"),
      doc.getAs[String]("description"),
      doc.getAs[Long]("_updated")
    )
  }

  implicit object ProductBSONWriter extends BSONDocumentWriter[Product] {
    def write(product: Product): BSONDocument = BSONDocument(
      "_id" -> product._id,
      "title" -> product.title,
      "description" -> product.description,
      "_updated" -> product._updated
    )
  }

}


