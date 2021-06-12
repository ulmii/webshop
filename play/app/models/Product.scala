package models


import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros}

case class Product(
                    id: Option[String] = Some(BSONObjectID.generate().stringify),
                    title: String,
                    description: Option[String],
                    price: BigDecimal,
                    category: Category,
                    imageUrl: String,
                    _updated: Option[Long]
                  )
  extends ApiModel[Product] {
  override protected def makeNew(updated: Option[Long]): Product = new Product(id = Some(BSONObjectID.generate().stringify), title, description, price, category, imageUrl, updated)
}

object Product {
  implicit val fmt: OFormat[Product] = Json.format[Product]

  implicit def idReader: BSONDocumentReader[BSONObjectID] = Macros.reader[BSONObjectID]

  implicit def idWriter: BSONDocumentWriter[BSONObjectID] = Macros.writer[BSONObjectID]

  implicit def productWriter: BSONDocumentWriter[Product] = Macros.writer[Product]

  implicit def productReader: BSONDocumentReader[Product] = Macros.reader[Product]
}


