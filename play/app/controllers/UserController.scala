package controllers

import javax.inject.{Inject, Singleton}
import models.User
import play.api.mvc._
import repository.UserRepository

import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(
                                implicit executionContext: ExecutionContext,
                                val userRepository: UserRepository,
                                implicit override val controllerComponents: ControllerComponents)
  extends CustomAbstractController[User] {

}
