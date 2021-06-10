package models

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros}

case class User(
                 id: Option[String] = Some(BSONObjectID.generate().stringify),
                 email: String,
                 password: Option[String] = None,
                 address: Option[Address],
                 basket: Option[Basket],
                 details: Option[UserDetails],
                 _updated: Option[Long]
               )
  extends ApiModel[User] with Identity {
  override protected def makeNew(updated: Option[Long]): User = new User(id = Some(BSONObjectID.generate().stringify), email, password, address, basket, details, updated)

  /**
   * Generates login info from email
   *
   * @return login info
   */
  def loginInfo: LoginInfo = LoginInfo(CredentialsProvider.ID, email)

  /**
   * Generates password info from password.
   *
   * @return password info
   */
  def passwordInfo: PasswordInfo = PasswordInfo(BCryptSha256PasswordHasher.ID, password.get)
}

object User {
  implicit val bLoginInfo: OFormat[LoginInfo] = Json.format[LoginInfo]
  implicit val fmt: OFormat[User] = Json.format[User]

  implicit def loginInfoReader: BSONDocumentReader[LoginInfo] = Macros.reader[LoginInfo]

  implicit def loginInfoWriter: BSONDocumentWriter[LoginInfo] = Macros.writer[LoginInfo]

  implicit def userWriter: BSONDocumentWriter[User] = Macros.writer[User]

  implicit def userReader: BSONDocumentReader[User] = Macros.reader[User]

}

