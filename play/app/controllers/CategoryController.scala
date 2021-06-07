package controllers

import javax.inject.{Inject, Singleton}
import models.Category
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}
import repository.{CategoryRepository, ProductRepository}

import scala.concurrent.ExecutionContext

@Singleton
class CategoryController @Inject()(
                                    implicit executionContext: ExecutionContext,
                                    val categoryRepository: CategoryRepository,
                                    val productRepository: ProductRepository,
                                    scc: SilhouetteControllerComponents)
  extends CustomAbstractController[Category] {
  def findProducts(name: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>

    productRepository.getProductsByCategory(name).map {
      product => Ok(Json.toJson(product))
    }
  }
}
