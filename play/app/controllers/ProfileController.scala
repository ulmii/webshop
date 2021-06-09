package controllers

import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import repository.UserRepository
import utils.auth.{DefaultEnv, WithProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfileController @Inject()(
                                   implicit executionContext: ExecutionContext,
                                   val userRepository: UserRepository,
                                   scc: SilhouetteControllerComponents)
  extends SilhouetteController(scc) {

  def findOne(): Action[AnyContent] = SecuredAction(WithProvider[DefaultEnv#A]("google")
    || WithProvider[DefaultEnv#A](CredentialsProvider.ID)).async(implicit request => {

    userService.retrieve(request.identity.loginInfo).flatMap {
      case Some(_) =>
        for {
          result <- userRepository.findOne(request.identity.id.get).map(user => Ok(Json.toJson(user)))
        } yield {
          result
        }
      case None => Future.successful(BadRequest(JsString("Could not find user")))
    }
  })

}
