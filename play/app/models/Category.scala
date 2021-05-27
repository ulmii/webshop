package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros}

case class Category(
                     id: Option[String] = Some(BSONObjectID.generate().stringify),
                     name: String,
                     _updated: Option[Long]
                   )
  extends ApiModel[Category] {
  override protected def makeNew(updated: Option[Long]): Category = new Category(id = Some(BSONObjectID.generate().stringify), name, updated)
}

object Category {
  implicit val fmt: OFormat[Category] = Json.format[Category]

  implicit def categoryWriter: BSONDocumentWriter[Category] = Macros.writer[Category]

  implicit def categoryReader: BSONDocumentReader[Category] = Macros.reader[Category]
}
