package controllers

import javax.inject.{Inject, Singleton}
import models.Basket
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import reactivemongo.bson.BSONObjectID
import repository.{BasketRepository, UserRepository}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class BasketController @Inject()(
                                  implicit executionContext: ExecutionContext,
                                  val basketRepository: BasketRepository,
                                  implicit override val controllerComponents: ControllerComponents)
  extends CustomAbstractController[Basket] {

  override def create(): Action[JsValue] = Action.async(controllerComponents.parsers.json)(implicit request => {

    request.body.validate[Basket].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      basket => {
        val objectIdTryResult = BSONObjectID.parse(basket.userId)
        objectIdTryResult match {
          case Success(objectId) => repository.update(objectId, basket)
            .map(_ => Created(Json.toJson(basket)))
          case Failure(_) => Future.successful(BadRequest("Cannot parse basket id"))
        }
      }
    )
  })

}
