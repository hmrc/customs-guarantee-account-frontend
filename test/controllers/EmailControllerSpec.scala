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

package controllers

import connectors.CustomsDataStoreConnector
import models.EmailUnverifiedResponse
import play.api.inject._
import services.MetricsReporterService
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import utils.SpecBase
import play.api.test.Helpers._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class EmailControllerSpec extends SpecBase {

  "EmailController" must {
    "return unverified email" in new Setup {
      running(app) {
        val connector = app.injector.instanceOf[CustomsDataStoreConnector]

        val result: Future[Option[String]] = connector.retrieveUnverifiedEmail(hc)

        await(result) mustBe expectedResult
      }
    }

    "return unverified email response" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)

        val result = route(app, request).value

        status(result) shouldBe OK
      }
    }
  }

  trait Setup {
    val expectedResult: Option[String]                     = Some("unverifiedEmail")
    private val mockHttpClient                             = mock[HttpClientV2]
    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]
    val requestBuilder: RequestBuilder                     = mock[RequestBuilder]

    val response: EmailUnverifiedResponse = EmailUnverifiedResponse(Some("unverifiedEmail"))

    when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

    when(requestBuilder.execute(any[HttpReads[EmailUnverifiedResponse]], any[ExecutionContext]))
      .thenReturn(Future.successful(response))

    when(mockHttpClient.get(any[URL]())(any)).thenReturn(requestBuilder)

    val app: Application = applicationBuilder
      .overrides(
        bind[MetricsReporterService].toInstance(mockMetricsReporterService),
        bind[HttpClientV2].toInstance(mockHttpClient),
        bind[RequestBuilder].toInstance(requestBuilder)
      )
      .build()
  }
}
