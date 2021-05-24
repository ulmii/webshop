package format

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

import scala.util.Try

object BSONObjectIDFormat extends Format[BSONObjectID] {

  def writes(objectId: BSONObjectID): JsValue = JsString(objectId.stringify)

  def reads(json: JsValue): JsResult[BSONObjectID] = json match {
    case JsString(x) =>
      val maybeOID: Try[BSONObjectID] = BSONObjectID.parse(x)
      if (maybeOID.isSuccess) JsSuccess(maybeOID.get) else {
        JsError("Expected ObjectId as JsString")
      }
    case _ => JsError("Expected ObjectId as JsString")
  }
}
