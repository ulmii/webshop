package models

import format.BSONObjectIDFormat
import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, _}

case class User(
                 _id: Option[BSONObjectID] = Some(BSONObjectID.generate()),
                 password: String,
                 email: String,
                 address: Option[Address],
                 basket: Option[Basket],
                 _updated: Option[Long]
               )
  extends ApiModel[User] {
  override protected def makeNew(updated: Option[Long]): User = new User(_id, password, email, address, basket, updated)
}

object User {
  implicit val objectIdFormat: BSONObjectIDFormat.type = BSONObjectIDFormat
  implicit val bBasket: OFormat[Basket] = Json.format[Basket]
  implicit val bAddress: OFormat[Address] = Json.format[Address]
  implicit val fmt: OFormat[User] = Json.format[User]

  implicit object UserBSONReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = User(
      doc.getAs[BSONObjectID]("_id"),
      null,
      doc.getAs[String]("email").get,
      doc.getAs[Address]("address"),
      doc.getAs[Basket]("basket"),
      doc.getAs[Long]("_updated")
    )
  }

  implicit object UserBSONWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument = BSONDocument(
      "_id" -> user._id,
      "password" -> user.password,
      "email" -> user.email,
      "address" -> user.address,
      "basket" -> user.basket,
      "_updated" -> user._updated
    )
  }

}

