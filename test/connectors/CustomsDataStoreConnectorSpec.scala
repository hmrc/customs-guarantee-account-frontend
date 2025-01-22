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

package connectors

import models.EmailUnverifiedResponse
import play.api.Application
import play.api.inject.bind
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import utils.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.Helpers.running
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class CustomsDataStoreConnectorSpec extends SpecBase {

  "retrieveUnverifiedEmail" must {

    "return unverified email" in new Setup {
      when(requestBuilder.execute(any[HttpReads[EmailUnverifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(EmailUnverifiedResponse(Some("unverified@email.com"))))

      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      val app: Application = applicationBuilder
        .overrides(
          bind[HttpClientV2].toInstance(mockHttpClient),
          bind[RequestBuilder].toInstance(requestBuilder)
        )
        .build()

      val connector: CustomsDataStoreConnector = app.injector.instanceOf[CustomsDataStoreConnector]

      running(app) {
        connector.retrieveUnverifiedEmail(hc) map { res =>
          res mustBe Some("unverified@email.com")
        }
      }
    }
  }

  trait Setup {
    val mockHttpClient: HttpClientV2   = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
  }
}
