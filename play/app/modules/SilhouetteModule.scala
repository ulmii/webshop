package modules

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.actions.{SecuredErrorHandler, UnsecuredErrorHandler}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{Clock, IDGenerator, PasswordHasherRegistry, _}
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.state.{CsrfStateItemHandler, CsrfStateSettings}
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, MongoAuthInfoDAO}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.typesafe.config.Config
import controllers.{DefaultSilhouetteControllerComponents, SilhouetteControllerComponents}
import javax.inject.Named
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.json.{Json, OFormat}
import play.api.libs.ws.WSClient
import play.api.mvc.Cookie
import play.modules.reactivemongo.ReactiveMongoApi
import services.UserIdentityService
import utils.auth.{CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}


class SilhouetteModule extends AbstractModule with ScalaModule {

  implicit val sameSiteReader: ValueReader[Option[Option[Cookie.SameSite]]] =
    (config: Config, path: String) => {
      if (config.hasPathOrNull(path)) {
        if (config.getIsNull(path))
          Some(None)
        else {
          Some(Cookie.SameSite.parse(config.getString(path)))
        }
      } else {
        None
      }
    }

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  /**
   * Provides the HTTP layer implementation.
   *
   * @param client Play's WS client.
   * @return The HTTP layer implementation.
   */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
   * Provides the Silhouette environment.
   *
   * @param userService          The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus             The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
                          userService: UserIdentityService,
                          authenticatorService: AuthenticatorService[JWTAuthenticator],
                          eventBus: EventBus): Environment[DefaultEnv] = {

    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
   * Provides the crypter for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the authenticator.
   */
  @Provides
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    new JcaCrypter(JcaCrypterSettings(configuration.underlying.getString("play.http.secret.key")))
  }

  /**
   * Provides AuthenticatorService
   *
   * @param crypter       encryption generic
   * @param idGenerator   id generator
   * @param configuration configuration set
   * @param clock         clock
   * @return AuthenticatorService implementation
   */
  @Provides
  def provideAuthenticatorService(
                                   crypter: Crypter,
                                   idGenerator: IDGenerator,
                                   configuration: Configuration,
                                   clock: Clock): AuthenticatorService[JWTAuthenticator] = {

    val encoder = new CrypterAuthenticatorEncoder(crypter)
    new JWTAuthenticatorService(JWTAuthenticatorSettings(
      fieldName = configuration.underlying.getString("silhouette.authenticator.headerName"),
      issuerClaim = configuration.underlying.getString("silhouette.authenticator.issuerClaim"),
      authenticatorExpiry = Duration(configuration.underlying.getString("silhouette.authenticator.authenticatorExpiry")).asInstanceOf[FiniteDuration],
      sharedSecret = configuration.underlying.getString("silhouette.authenticator.sharedSecret")

    ), None, encoder, idGenerator, clock)
  }

  @Provides
  def providePasswordInfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[PasswordInfo] = {
    implicit lazy val format: OFormat[PasswordInfo] = Json.format[PasswordInfo]
    new MongoAuthInfoDAO[PasswordInfo](reactiveMongoApi, config)
  }

  @Provides
  def provideOAuth2InfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[OAuth2Info] = {
    implicit lazy val format: OFormat[OAuth2Info] = Json.format[OAuth2Info]
    new MongoAuthInfoDAO[OAuth2Info](reactiveMongoApi, config)
  }

  /**
   * Provides the auth info repository.
   *
   * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
   * @return The auth info repository instance.
   */
  @Provides
  def provideAuthInfoRepository(
                                 passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
                                 oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info],
                               ): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoDAO, oauth2InfoDAO)
  }

  /**
   * Provides the password hasher registry.
   *
   * @return The password hasher registry.
   */
  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

  /**
   * Provides the credentials provider.
   *
   * @param authInfoRepository     The auth info repository implementation.
   * @param passwordHasherRegistry The password hasher registry.
   * @return The credentials provider.
   */
  @Provides
  def provideCredentialsProvider(
                                  authInfoRepository: AuthInfoRepository,
                                  passwordHasherRegistry: PasswordHasherRegistry): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }

  /**
   * Provides silhouette components
   *
   * @param components silhouette components implementation
   * @return silhouette components implementation
   */
  @Provides
  def providesSilhouetteComponents(components: DefaultSilhouetteControllerComponents): SilhouetteControllerComponents = {
    components
  }

  @Provides
  def provideSocialProviderRegistry(googleProvider: GoogleProvider): SocialProviderRegistry =
    SocialProviderRegistry(Seq(googleProvider))

  @Provides
  def provideGoogleProvider(httpLayer: HTTPLayer,
                            socialStateHandler: SocialStateHandler,
                            configuration: Configuration): GoogleProvider =
    new GoogleProvider(httpLayer, socialStateHandler, configuration.underlying.as[OAuth2Settings]("silhouette.google"))

  @Provides
  def provideSocialStateHandler(@Named("social-state-signer") signer: Signer,
                                csrfStateItemHandler: CsrfStateItemHandler): SocialStateHandler =
    new DefaultSocialStateHandler(Set(csrfStateItemHandler), signer)

  @Provides
  @Named("social-state-signer")
  def provideSocialStateSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.socialStateHandler.signer")
    new JcaSigner(config)
  }

  @Provides
  def provideCsrfStateItemHandler(idGenerator: IDGenerator,
                                  @Named("csrf-state-item-signer") signer: Signer,
                                  configuration: Configuration): CsrfStateItemHandler = {
    val settings = configuration.underlying.as[CsrfStateSettings]("silhouette.csrfStateItemHandler")
    new CsrfStateItemHandler(settings, idGenerator, signer)
  }

  @Provides
  @Named("csrf-state-item-signer")
  def provideCSRFStateItemSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.csrfStateItemHandler.signer")
    new JcaSigner(config)
  }
}
