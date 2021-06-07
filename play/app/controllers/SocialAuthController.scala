package controllers

import java.time.Instant

import com.mohiva.play.silhouette.api.LoginEvent
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers._
import javax.inject.Inject
import models.User
import play.api.mvc.{Action, AnyContent, Cookie, Request}
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRF, CSRFAddToken}
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
            // Check if user already exists
//            user <- userService.createAndGet(User(id = Some(BSONObjectID.generate().stringify), profile.email.get, None, None, None, None, Some(Instant.now().getEpochSecond)))
            authenticator <- authenticatorService.create(profile.loginInfo)
            token <- authenticatorService.init(authenticator)
            result <- authenticatorService.embed(token, Ok)
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
