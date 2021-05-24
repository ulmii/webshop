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
                                  val repository: BasketRepository,
                                  val userRepository: UserRepository,
                                  val controllerComponents: ControllerComponents)
  extends BaseController {

  def findAll(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    repository.findAll().map(baskets => Ok(Json.toJson(baskets)))
  }

  def findOne(id: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val objectIdTryResult = BSONObjectID.parse(id)
    objectIdTryResult match {
      case Success(objectId) => repository.findUserBasket(objectId).map {
        basket => Ok(Json.toJson(basket))
      }
      case Failure(_) => Future.successful(BadRequest("Cannot parse basket id"))
    }
  }

  def create(): Action[JsValue] = Action.async(controllerComponents.parsers.json)(implicit request => {

    request.body.validate[Basket].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      basket => {
        val objectIdTryResult = BSONObjectID.parse(basket.userId)
        objectIdTryResult match {
          case Success(objectId) => repository.updateUserBasket(objectId, basket)
            .map(_ => Created(Json.toJson(basket)))
          case Failure(_) => Future.successful(BadRequest("Cannot parse basket id"))
        }
      }
    )
  })

  def delete(id: String): Action[AnyContent] = Action.async { implicit request => {
    val objectIdTryResult = BSONObjectID.parse(id)
    objectIdTryResult match {
      case Success(objectId) => repository.delete(objectId).map {
        _ => NoContent
      }
      case Failure(_) => Future.successful(BadRequest("Cannot parse basket id"))
    }
  }
  }

}
