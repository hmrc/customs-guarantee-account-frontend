/*
 * Copyright 2021 HM Revenue & Customs
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

package connectors

import config.AppConfig
import models.request.{GuaranteeTransactionsRequest, IdentifierRequest}
import models.{GuaranteeAccount, GuaranteeTransaction, RequestDates}
import org.slf4j.LoggerFactory
import play.api.mvc.AnyContent
import repositories.CacheRepository
import services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsFinancialsApiConnector @Inject()(httpClient: HttpClient,
                                              appConfig: AppConfig,
                                              metricsReporter: MetricsReporterService,
                                              cacheRepository: CacheRepository)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  private val baseUrl = appConfig.customsFinancialsApi
  private val accountsUrl = s"$baseUrl/eori/accounts"
  private val retrieveOpenGuaranteeTransactionsDetailUrl = s"$baseUrl/account/guarantee/open-transactions-detail"

  def getGuaranteeAccount(eori: String)(implicit hc: HeaderCarrier, request: IdentifierRequest[AnyContent]): Future[Option[GuaranteeAccount]] = {
    val requestDetail = AccountsRequestDetail(eori, None, None, None)
    val accountsAndBalancesRequest = AccountsAndBalancesRequestContainer(
      AccountsAndBalancesRequest(AccountsRequestCommon.generate(), requestDetail)
    )

    metricsReporter.withResponseTimeLogging("customs-financials-api.get.accounts") {
      httpClient.POST[AccountsAndBalancesRequestContainer, AccountsAndBalancesResponseContainer](accountsUrl, accountsAndBalancesRequest).map(_.toGuaranteeAccounts)
    }
  }.map(_.find(_.owner == request.eori))

  def retrieveOpenGuaranteeTransactionsDetail(gan: String)(implicit hc: HeaderCarrier): Future[Either[GuaranteeResponses, Seq[GuaranteeTransaction]]] = {
    val openGuaranteeTransactionsRequest = GuaranteeTransactionsRequest(gan, openItems = true, None)

    cacheRepository.get(gan).flatMap {
      case Some(value) => Future.successful(Right(value))
      case None =>
        httpClient.POST[GuaranteeTransactionsRequest, Seq[GuaranteeTransaction]](
          retrieveOpenGuaranteeTransactionsDetailUrl,
          openGuaranteeTransactionsRequest
        ).flatMap { transactions =>
          cacheRepository.set(gan, transactions).map { successfulWrite =>
            if (!successfulWrite) {
              logger.error("Failed to store data in the session cache defaulting to the api response")
            }
            Right(transactions)
          }
        }
    }.recover {
      case UpstreamErrorResponse(_, 413, _, _) => logger.error(s"Entity too large to download"); Left(TooManyTransactionsRequested)
      case UpstreamErrorResponse(_, 404, _, _) => logger.info(s"No data found"); Left(NoTransactionsAvailable)
      case e => logger.error(s"Unable to download CSV :${e.getMessage}"); Left(UnknownException)
    }
  }

  def retrieveRequestedGuaranteeTransactionsDetail(gan: String, onlyOpenItems: Boolean, from: LocalDate, to: LocalDate)(implicit hc: HeaderCarrier): Future[Either[GuaranteeResponses, Seq[GuaranteeTransaction]]] = {
    val openGuaranteeTransactionsRequest = GuaranteeTransactionsRequest(gan, onlyOpenItems, Some(RequestDates(from, to)))
    httpClient.POST[GuaranteeTransactionsRequest, Seq[GuaranteeTransaction]](
      retrieveOpenGuaranteeTransactionsDetailUrl,
      openGuaranteeTransactionsRequest).map(Right(_))
  }.recover {
    case UpstreamErrorResponse(_, 413, _, _) => logger.error(s"Entity too large to download"); Left(TooManyTransactionsRequested)
    case UpstreamErrorResponse(_, 404, _, _) => logger.info(s"No data found"); Left(NoTransactionsAvailable)
    case e => logger.error(s"Unable to download CSV :${e.getMessage}"); Left(UnknownException)
  }
}


sealed trait GuaranteeResponses

case object NoTransactionsAvailable extends GuaranteeResponses

case object TooManyTransactionsRequested extends GuaranteeResponses

case object UnknownException extends GuaranteeResponses

