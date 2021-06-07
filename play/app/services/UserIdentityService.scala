package services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import javax.inject.{Inject, Singleton}
import models.User
import reactivemongo.api.commands.WriteResult
import repository.UserRepository

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UserIdentityService @Inject()(implicit ec: ExecutionContext, userRepository: UserRepository)
  extends IdentityService[User] {

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    userRepository.findUser(loginInfo)
  }

  def updateUser(user: User): Unit = {
    userRepository.update(user.id.get, user)
  }

  def create(user: User): Future[WriteResult] = {
    userRepository.create(user)
  }

  def createAndGet(user: User): Future[User] = {
    userRepository.createAndGet(user)
  }
}
