package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

case class Category(
                     _id: Option[BSONObjectID] = Some(BSONObjectID.generate()),
                     name: String,
                     _updated: Option[Long]
                   )
  extends ApiModel[Category] {
  override protected def makeNew(updated: Option[Long]): Category = new Category(_id, name, updated)
}

object Category {
  implicit val bObjectIdFormat: OFormat[BSONObjectID] = Json.format[BSONObjectID]
  implicit val fmt: OFormat[Category] = Json.format[Category]

  implicit object ProductBSONReader extends BSONDocumentReader[Category] {
    def read(doc: BSONDocument): Category = Category(
      doc.getAs[BSONObjectID]("_id"),
      doc.getAs[String]("name").get,
      doc.getAs[Long]("_updated")
    )
  }

  implicit object ProductBSONWriter extends BSONDocumentWriter[Category] {
    def write(product: Category): BSONDocument = BSONDocument(
      "_id" -> product._id,
      "name" -> product.name,
      "_updated" -> product._updated
    )
  }

}
