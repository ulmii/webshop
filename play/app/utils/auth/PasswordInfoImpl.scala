package utils.auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import services.UserIdentityService

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
 * PasswordInfo repository.
 */
class PasswordInfoImpl @Inject()(userService: UserIdentityService)(implicit val classTag: ClassTag[PasswordInfo], ec: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo] {

  /**
   * Finds passwordInfo for specified loginInfo
   *
   * @param loginInfo user's email
   * @return user's hashed password
   */
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = userService.retrieve(loginInfo).map(_.map(_.passwordInfo))

  /**
   * Adds new passwordInfo for specified loginInfo
   *
   * @param loginInfo    user's email
   * @param passwordInfo user's hashed password
   */
  override def add(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = update(loginInfo, passwordInfo)

  /**
   * Updates passwordInfo for specified loginInfo
   *
   * @param loginInfo    user's email
   * @param passwordInfo user's hashed password
   */
  override def update(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = userService.retrieve(loginInfo).flatMap {
    case Some(user) => {
      userService.updateUser(user.copy(password = passwordInfo.password))
      Future.successful(passwordInfo)
    }
    case None => Future.failed(new Exception("user not found"))
  }

  /**
   * Adds new passwordInfo for specified loginInfo
   *
   * @param loginInfo    user's email
   * @param passwordInfo user's hashed password
   */
  override def save(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = update(loginInfo, passwordInfo)

  /**
   * Removes passwordInfo for specified loginInfo
   *
   * @param loginInfo user's email
   */
  override def remove(loginInfo: LoginInfo): Future[Unit] = update(loginInfo, PasswordInfo("", "")).map(_ => ())
}

