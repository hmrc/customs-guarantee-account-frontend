/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}


@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {
  val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  lazy val appName: String = config.get[String]("appName")
  lazy val loginUrl: String = config.get[String]("urls.login")
  lazy val loginContinueUrl: String = config.get[String]("urls.loginContinue")
  lazy val signOutUrl: String = config.get[String]("urls.signOut")
  lazy val feedbackService = config.getOptional[String]("feedback.url").getOrElse("/feedback") +
    config.getOptional[String]("feedback.source").getOrElse("/CDS-FIN")

  lazy val guaranteeAccountGuidanceUrl: String = config.get[String]("urls.guaranteeAccountGuidanceUrl")

  val customsFinancialsApi: String = servicesConfig.baseUrl("customs-financials-api") +
    config.getOptional[String]("customs-financials-api.context").getOrElse("/customs-financials-api")

  lazy val mrnEncryptionKey = config.get[String]("")

  lazy val numberOfItemsPerPage: Int = config.get[Int]("application.guarantee-account.numberOfItemsPerPage")
  lazy val guaranteeAccountInterval: Int = config.get[Int]("application.guarantee-account.updateTime.intervalMilliseconds")
  lazy val guaranteeAccountTimeout: Int = config.get[Int]("application.guarantee-account.updateTime.timeoutMilliseconds")
  lazy val registerCdsUrl = config.get[String]("urls.cdsRegisterUrl")
  lazy val subscribeCdsUrl = config.get[String]("urls.cdsSubscribeUrl")
  lazy val customsFinancialsFrontendHomepage = config.get[String]("urls.customsFinancialsHomepage")
  lazy val applicationStatusCdsUrl = config.get[String]("urls.applicationStatusUrl")
  lazy val govUkHomepage = config.get[String]("urls.govUkHome")

  lazy val timeout: Int = config.get[Int]("timeout.timeout")
  lazy val countdown: Int = config.get[Int]("timeout.countdown")
  lazy val fixedDateTime = config.get[Boolean]("features.fixed-systemdate-for-tests")

}
