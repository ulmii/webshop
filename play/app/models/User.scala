package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

case class User(
                 _id: Option[String],
                 username: String,
                 password: String,
                 email: String,
                 address: Option[Address],
                 _updated: Option[Long]
               )
  extends ApiModel[User] {
  override protected def makeNew(updated: Option[Long]): User = new User(_id, username, password, email, address, updated)
}

object User {
  implicit val bObjectIdFormat: OFormat[BSONObjectID] = Json.format[BSONObjectID]
  implicit val bAddress: OFormat[Address] = Json.format[Address]
  implicit val fmt: OFormat[User] = Json.format[User]

  implicit object UserBSONReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = User(
      doc.getAs[BSONObjectID]("_id").map(dt => dt.stringify),
      doc.getAs[String]("username").get,
      null,
      doc.getAs[String]("email").get,
      doc.getAs[Address]("address"),
      doc.getAs[Long]("_updated")
    )
  }

  implicit object UserBSONWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument = BSONDocument(
      "_id" -> user._id,
      "username" -> user.username,
      "password" -> user.password,
      "email" -> user.email,
      "address" -> user.address,
      "_updated" -> user._updated
    )
  }

}

