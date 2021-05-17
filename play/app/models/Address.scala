package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

case class Address(
                    country: String,
                  )

object Address {
  implicit val fmt: OFormat[Address] = Json.format[Address]

  implicit object AddressBSONReader extends BSONDocumentReader[Address] {
    def read(doc: BSONDocument): Address = Address(
      doc.getAs[String]("country").get
    )
  }

  implicit object AddressBSONWriter extends BSONDocumentWriter[Address] {
    def write(address: Address): BSONDocument = BSONDocument(
      "country" -> address.country,
    )
  }

}

