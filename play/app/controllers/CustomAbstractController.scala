package controllers

import play.api.libs.json.{JsValue, Json, Reads, Writes}
import play.api.mvc._
import reactivemongo.api.bson.BSONObjectID
import repository.Repository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class CustomAbstractController[T: Writes : Reads](
                                                            implicit executionContext: ExecutionContext,
                                                            val repository: Repository[T],
                                                            scc: SilhouetteControllerComponents
                                                          ) extends SilhouetteController(scc) {
  val ERROR_ID = "Cannot parse id"

  def findAll(): Action[AnyContent] = Action.async(implicit request => {
    repository.findAll()
      .map(users => Ok(Json.toJson(users)))
  }
  )

  def findOne(id: String): Action[AnyContent] = Action.async(implicit request => {

    val objectIdTryResult = BSONObjectID.parse(id)
    objectIdTryResult match {
      case Success(objectId) => repository.findOne(id).map(model => Ok(Json.toJson(model)))
      case Failure(_) => Future.successful(BadRequest(ERROR_ID))
    }
  })

  def create(): Action[JsValue] = Action.async(controllerComponents.parsers.json)(implicit request => {

    request.body.validate[T].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      model =>
        repository.create(model).map(_ => Created(Json.toJson(model)))
    )
  })

  def update(id: String): Action[JsValue] = Action.async(controllerComponents.parsers.json)(implicit request => {

    request.body.validate[T].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      model => {
        val objectIdTryResult = BSONObjectID.parse(id)
        objectIdTryResult match {
          case Success(objectId) => repository.update(id, model).map(result => Ok(Json.toJson(result.ok)))
          case Failure(_) => Future.successful(BadRequest(ERROR_ID))
        }
      }
    )
  })

  def delete(id: String): Action[AnyContent] = Action.async(implicit request => {

    val objectIdTryResult = BSONObjectID.parse(id)
    objectIdTryResult match {
      case Success(objectId) => repository.delete(id).map(_ => NoContent)
      case Failure(_) => Future.successful(BadRequest(ERROR_ID))
    }
  })
}
