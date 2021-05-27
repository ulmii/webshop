package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

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

  implicit def userDetailsWriter: BSONDocumentWriter[UserDetails] = Macros.writer[UserDetails]

  implicit def userDetailsReader: BSONDocumentReader[UserDetails] = Macros.reader[UserDetails]

}
