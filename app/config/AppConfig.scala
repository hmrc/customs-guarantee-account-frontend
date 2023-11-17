/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

  lazy val viewGeneralGuaranteeAccountLink: String = config.get[String]("urls.viewGeneralGuaranteeAccountLink")

  val customsFinancialsApi: String = servicesConfig.baseUrl("customs-financials-api") +
    config.getOptional[String]("customs-financials-api.context").getOrElse("/customs-financials-api")

  lazy val customsDataStore: String = servicesConfig.baseUrl("customs-data-store") +
    config.get[String]("microservice.services.customs-data-store.context")

  lazy val mrnEncryptionKey = config.get[String]("")
  lazy val numberOfItemsPerPage: Int = config.get[Int]("application.guarantee-account.numberOfItemsPerPage")
  lazy val guaranteeAccountInterval: Int = config.get[Int]("application.guarantee-account.updateTime.intervalMilliseconds")
  lazy val guaranteeAccountTimeout: Int = config.get[Int]("application.guarantee-account.updateTime.timeoutMilliseconds")
  lazy val registerCdsUrl = config.get[String]("urls.cdsRegisterUrl")
  lazy val subscribeCdsUrl = config.get[String]("urls.cdsSubscribeUrl")
  lazy val customsFinancialsFrontendHomepage = config.get[String]("urls.customsFinancialsHomepage")
  lazy val govUkHomepage = config.get[String]("urls.govUkHome")
  lazy val timeout: Int = config.get[Int]("timeout.timeout")
  lazy val countdown: Int = config.get[Int]("timeout.countdown")
  lazy val fixedDateTime = config.get[Boolean]("features.fixed-systemdate-for-tests")
  lazy val helpMakeGovUkBetterUrl: String = config.get[String]("urls.helpMakeGovUkBetterUrl")
  lazy val guaranteeAccountGuidanceUrl: String = config.get[String]("urls.guaranteeAccountGuidanceUrl")
  lazy val emailFrontendUrl: String = config.get[String]("microservice.services.customs-email-frontend.url")
}
