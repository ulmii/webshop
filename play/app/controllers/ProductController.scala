package controllers

import java.time.Instant

import javax.inject.{Inject, Singleton}
import models.{Category, Product}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import reactivemongo.bson.BSONObjectID
import repository.{CategoryRepository, ProductRepository}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ProductController @Inject()(
                                   implicit executionContext: ExecutionContext,
                                   val productRepository: ProductRepository,
                                   val categoryRepository: CategoryRepository,
                                   val controllerComponents: ControllerComponents)
  extends BaseController {

  def findAll(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    productRepository.findAll().map {
      products => Ok(Json.toJson(products))
    }
  }

  def findOne(id: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val objectIdTryResult = BSONObjectID.parse(id)
    objectIdTryResult match {
      case Success(objectId) => productRepository.findOne(objectId).map {
        product => Ok(Json.toJson(product))
      }
      case Failure(_) => Future.successful(BadRequest("Cannot parse product id"))
    }
  }

  def create(): Action[JsValue] = Action.async(controllerComponents.parsers.json) { implicit request => {

    request.body.validate[Product].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      product => {
        categoryRepository.findByName("nowe").map(cat => if (cat.isEmpty) {
          categoryRepository.create(new Category(name = "nowe", _updated = Some(Instant.now().getEpochSecond)))
        })

        productRepository.create(product).map {
          _ => Created(Json.toJson(product))
        }
      }
    )
  }
  }

  def update(id: String): Action[JsValue] = Action.async(controllerComponents.parsers.json) { implicit request => {
    request.body.validate[Product].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      product => {
        val objectIdTryResult = BSONObjectID.parse(id)
        objectIdTryResult match {
          case Success(objectId) => productRepository.update(objectId, product).map {
            result => Ok(Json.toJson(result.ok))
          }
          case Failure(_) => Future.successful(BadRequest("Cannot parse product id"))
        }
      }
    )
  }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request => {
    val objectIdTryResult = BSONObjectID.parse(id)
    objectIdTryResult match {
      case Success(objectId) => productRepository.delete(objectId).map {
        _ => NoContent
      }
      case Failure(_) => Future.successful(BadRequest("Cannot parse product id"))
    }
  }
  }
}
