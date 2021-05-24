package controllers

import javax.inject.{Inject, Singleton}
import models.Basket
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import reactivemongo.api.bson.BSONObjectID
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
    repository.findAll().map {
      baskets => Ok(Json.toJson(baskets))
    }
  }

  def findOne(username: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    repository.findUserBasket(username).map {
      basket => Ok(Json.toJson(basket))
    }
  }

  def create(): Action[JsValue] = Action.async(controllerComponents.parsers.json) { implicit request => {
//    request.body.validate[Basket].fold(
//      _ => Future.successful(BadRequest("Cannot parse basket")),
//      basket => {
//        return userRepository.findByUsername(basket.username)
//          .map(user => {
//            val updatedUser = user.get.copy(basket = Some(basket))
//            return basket => Ok(Json.toJson(basket))
//            val objectIdTryResult = BSONObjectID.parse(updatedUser._id.get)
//            objectIdTryResult match {
//              case Success(objectId) => userRepository.update(objectId, updatedUser).map {
//                _ => Created(Json.toJson(basket))
//              }
//              case Failure(_) => Future.successful(BadRequest("Cannot find user from basket"))
//            }
//          })
//      }
//    )

    request.body.validate[Basket].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      basket =>
        userRepository.findByUsername(basket.username).map(user => {
          val updatedUser = user.get.copy(basket = Some(basket))
          repository.update(basket.username, updatedUser)

          Created(Json.toJson(repository.findUserBasket(basket.username)))
        })
    )
  }
  }

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
