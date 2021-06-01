package utils.auth

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import javax.inject.Inject
import play.api
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.Future

/**
 * Custom secured error handler.
 *
 * @param messagesApi The Play messages API.
 */
class CustomSecuredErrorHandler @Inject()(val messagesApi: MessagesApi) extends SecuredErrorHandler with I18nSupport {

  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @return The result to send to the client.
   */
  override def onNotAuthenticated(implicit request: RequestHeader): Future[mvc.Results.Status] = {
    Future.successful(Unauthorized)
  }

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(implicit request: RequestHeader): Future[api.mvc.Results.Status] = {
    Future.successful(Forbidden)
    //    Future.successful(Redirect(controllers.routes.SignInController.view()).flashing("error" -> Messages("access.denied")))
  }
}