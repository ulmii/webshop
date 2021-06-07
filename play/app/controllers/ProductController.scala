package controllers

import javax.inject.{Inject, Singleton}
import models.Product
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import repository.{CategoryRepository, ProductRepository}

import scala.concurrent.{ExecutionContext, Future}

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

    productRepository.findOne(id).map {
      product => Ok(Json.toJson(product))
    }
  }

  def create(): Action[JsValue] = Action.async(controllerComponents.parsers.json) { implicit request => {

    request.body.validate[Product].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      product => {
        categoryRepository.createIfNone(product)

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
        categoryRepository.createIfNone(product)

        productRepository.update(id, product).map {
          result => Ok(Json.toJson(result.ok))
        }
      }
    )
  }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request => {

    productRepository.delete(id).map {
      _ => NoContent
    }
  }
  }
}
