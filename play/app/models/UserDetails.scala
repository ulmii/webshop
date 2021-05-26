package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

case class UserDetails(
                        userId: String,
                        firstName: Option[String],
                        lastName: Option[String],
                        _updated: Option[Long]
                      )
  extends NestedApiModel[UserDetails] {
  override protected def makeNew(updated: Option[Long]): UserDetails = new UserDetails(userId, firstName, lastName, updated)
}

object UserDetails {
  implicit val fmt: OFormat[UserDetails] = Json.format[UserDetails]

  implicit object UserDetailsBSONReader extends BSONDocumentReader[UserDetails] {
    def read(doc: BSONDocument): UserDetails = UserDetails(
      doc.getAs[String]("userId").get,
      doc.getAs[String]("firstName"),
      doc.getAs[String]("name"),
      doc.getAs[Long]("_updated")
    )
  }

  implicit object UserDetailsBSONWriter extends BSONDocumentWriter[UserDetails] {
    def write(details: UserDetails): BSONDocument = BSONDocument(
      "userId" -> details.userId,
      "firstName" -> details.firstName,
      "lastName" -> details.lastName,
      "_updated" -> details._updated
    )
  }

}
