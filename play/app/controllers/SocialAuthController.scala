package controllers

import java.time.Instant

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers._
import javax.inject.Inject
import models.User
import play.api.mvc.{Action, AnyContent, Request}
import play.filters.csrf.CSRFAddToken
import reactivemongo.api.bson.BSONObjectID
import services.UserIdentityService

import scala.concurrent.{ExecutionContext, Future}

class SocialAuthController @Inject()(scc: DefaultSilhouetteControllerComponents,
                                     addToken: CSRFAddToken,
                                     userService: UserIdentityService)(implicit ex: ExecutionContext)
  extends SilhouetteController(scc) {

  def authenticate(provider: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            loginInfo = LoginInfo(profile.loginInfo.providerID, profile.email.get)
            // Create user if doesn't exist
            _ <- userService.retrieve(loginInfo).flatMap {
              case None =>
                userService.create(User(id = Some(BSONObjectID.generate().stringify), profile.email.get, None, None, None, None, Some(Instant.now().getEpochSecond)))
              case _ => Future.successful()
            }
            authenticator <- authenticatorService.create(loginInfo)
            authToken <- authenticatorService.init(authenticator)
            result <- authenticatorService.embed(authToken, Ok)
          } yield {
            logger.debug(s"User ${profile.loginInfo.providerKey} signed success")
            result
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Forbidden("Forbidden")
    }
  }
}
