package controllers

import javax.inject.{Inject, Singleton}
import models.User
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import repository.UserRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(
                                implicit executionContext: ExecutionContext,
                                val userRepository: UserRepository,
                                scc: SilhouetteControllerComponents)
  extends CustomAbstractController[User] {

  override def findAll(): Action[AnyContent] = Action.async(implicit request => {
    repository.findAll()
      .map(users => Ok(Json.toJson(users.map(u => u.copy(password = None)))))
  }
  )

  override def findOne(id: String): Action[AnyContent] = Action.async(implicit request => {

    repository.findOne(id).map(user => Ok(Json.toJson(user.map(u => u.copy(password = None)))))
  })

  override def create(): Action[JsValue] = Action.async(controllerComponents.parsers.json)(implicit request => {

    request.body.validate[User].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      user =>
        repository.create(user).map(_ => Created(Json.toJson(user.copy(password = None))))
    )
  })

  override def update(id: String): Action[JsValue] = Action.async(controllerComponents.parsers.json)(implicit request => {

    request.body.validate[User].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      user => {
        repository.update(id, user).map(result => Ok(Json.toJson(result.ok)))
      }
    )
  })

  override def delete(id: String): Action[AnyContent] = Action.async(implicit request => {

    repository.delete(id).map(_ => NoContent)
  })

}
