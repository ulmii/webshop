package controllers

import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.{Inject, Singleton}
import models.Basket
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.mvc._
import repository.BasketRepository
import utils.auth.{DefaultEnv, WithProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BasketController @Inject()(
                                  implicit executionContext: ExecutionContext,
                                  val basketRepository: BasketRepository,
                                  scc: SilhouetteControllerComponents)
  extends CustomAbstractController[Basket] {

  override def create(): Action[JsValue] = SecuredAction(WithProvider[DefaultEnv#A](CredentialsProvider.ID)).async(scc.parsers.json)(implicit request => {

    request.body.validate[Basket].fold(
      _ => Future.successful(BadRequest("Cannot parse request")),
      basket => {
        userService.retrieve(request.identity.loginInfo).flatMap {
          case Some(_) =>
            for {
              result <- repository.update(request.identity.id.get, basket.copy(userId = request.identity.id))
                .map(_ => Created(Json.toJson(basket.copy(userId = request.identity.id))))
            } yield {
              result
            }
          case None => Future.successful(BadRequest(JsString("Could not find user")))
        }
      }
    )
  })

  def findOne(): Action[AnyContent] = SecuredAction(WithProvider[DefaultEnv#A](CredentialsProvider.ID)).async(implicit request => {

    userService.retrieve(request.identity.loginInfo).flatMap {
      case Some(_) =>
        for {
          result <- repository.findOne(request.identity.id.get).map(basket => Ok(Json.toJson(basket)))
        } yield {
          result
        }
      case None => Future.successful(BadRequest(JsString("Could not find user")))
    }
  })

}
