package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}


case class Basket(
                   userId: Option[String] = None,
                   products: Option[Seq[Product]],
                   _updated: Option[Long]
                 )

object Basket {
  implicit val fmt: OFormat[Basket] = Json.format[Basket]

  implicit def basketWriter: BSONDocumentWriter[Basket] = Macros.writer[Basket]

  implicit def basketReader: BSONDocumentReader[Basket] = Macros.reader[Basket]
}




