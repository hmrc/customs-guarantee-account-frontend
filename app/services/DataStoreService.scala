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

package services

import config.AppConfig
import models.domain.EORI
import models.{EmailResponses, UnverifiedEmail}
import domain.UndeliverableInformation
import play.api.Logger
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataStoreService @Inject() (httpClient: HttpClientV2, metricsReporter: MetricsReporterService)(implicit
  appConfig: AppConfig,
  ec: ExecutionContext
) {

  val log: Logger = Logger(this.getClass)

  def getEmail(implicit hc: HeaderCarrier): Future[Either[EmailResponses, Email]] = {
    val dataStoreEndpoint = s"${appConfig.customsDataStore}/eori/verified-email"

    metricsReporter.withResponseTimeLogging("customs-data-store.get.email") {
      httpClient
        .get(url"$dataStoreEndpoint")
        .execute[EmailResponse]
        .map {
          case EmailResponse(Some(address), _, None) => Right(Email(address))
          case _                                     => Left(UnverifiedEmail)
        }
        .recover { case UpstreamErrorResponse(_, NOT_FOUND, _, _) =>
          Left(UnverifiedEmail)
        }
    }
  }
}

case class EmailResponse(
  address: Option[String],
  imestamp: Option[String],
  undeliverable: Option[UndeliverableInformation]
)

object EmailResponse {
  implicit val format: OFormat[EmailResponse] = Json.format[EmailResponse]
}
