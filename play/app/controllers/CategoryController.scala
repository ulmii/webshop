package controllers

import javax.inject.{Inject, Singleton}
import models.Category
import play.api.mvc.ControllerComponents
import repository.CategoryRepository

import scala.concurrent.ExecutionContext

@Singleton
class CategoryController @Inject()(
                                    implicit executionContext: ExecutionContext,
                                    val categoryRepository: CategoryRepository,
                                    scc: SilhouetteControllerComponents)
  extends CustomAbstractController[Category] {

}
