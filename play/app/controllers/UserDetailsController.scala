package controllers

import javax.inject.{Inject, Singleton}
import models.UserDetails
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import reactivemongo.api.bson.BSONObjectID
import repository.UserDetailsRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class UserDetailsController @Inject()(
                                       implicit executionContext: ExecutionContext,
                                       userDetailsRepository: UserDetailsRepository,
                                       scc: SilhouetteControllerComponents)
  extends CustomAbstractController[UserDetails] {

  override def create(): Action[JsValue] = Action.async(controllerComponents.parsers.json)(implicit request => {

    request.body.validate[UserDetails].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      userDetails => {
        val objectIdTryResult = BSONObjectID.parse(userDetails.userId)
        objectIdTryResult match {
          case Success(objectId) => repository.update(userDetails.userId, userDetails)
            .map(_ => Created(Json.toJson(userDetails)))
          case Failure(_) => Future.successful(BadRequest("Cannot parse userDetails id"))
        }
      }
    )
  })

}
