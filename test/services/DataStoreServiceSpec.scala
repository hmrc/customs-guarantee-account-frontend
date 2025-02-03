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

import domain.{UndeliverableInformation, UndeliverableInformationEvent}
import models.UnverifiedEmail
import org.joda.time.DateTime
import org.mockito.invocation.InvocationOnMock
import play.api.{Application, inject}
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.{HttpReads, ServiceUnavailableException, UpstreamErrorResponse}
import utils.SpecBase
import utils.Utils.emptyString
import play.api.http.Status.NOT_FOUND
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class DataStoreServiceSpec extends SpecBase {

  "Data store service" should {

    "return existing email" in new Setup {
      val jsonResponse: String = """{"address":"someemail@mail.com"}""".stripMargin

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(Json.parse(jsonResponse).as[EmailResponse]))

      when(mockHttpClient.get(any[URL]())(any)).thenReturn(requestBuilder)

      running(application) {
        val response = service.getEmail
        val result   = await(response)

        result mustBe Right(Email("someemail@mail.com"))
      }
    }

    "return unverified email" in new Setup {
      val hundred = 100

      val undeliverableEventData: UndeliverableInformationEvent = UndeliverableInformationEvent(
        "someid",
        "someevent",
        "someemail",
        emptyString,
        Some(hundred),
        Some("sample"),
        "sample"
      )

      val emailResponse: EmailResponse = EmailResponse(
        Some("sample@email.com"),
        Some("time"),
        Some(
          UndeliverableInformation(
            "subject-example",
            "ex-event-id-01",
            "ex-group-id-01",
            DateTime.now(),
            undeliverableEventData
          )
        )
      )

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailResponse))

      when(mockHttpClient.get(any[URL]())(any)).thenReturn(requestBuilder)

      running(application) {
        val response = service.getEmail
        val result   = await(response)

        result mustBe Left(UnverifiedEmail)
      }
    }

    "return a UnverifiedEmail on error response" in new Setup {
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("NoData", NOT_FOUND, NOT_FOUND)))

      when(mockHttpClient.get(any[URL]())(any)).thenReturn(requestBuilder)

      running(application) {
        val response = service.getEmail

        await(response) mustBe Left(UnverifiedEmail)
      }
    }

    "throw service unavailable" in new Setup {
      running(application) {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

        when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
          .thenReturn(Future.failed(new ServiceUnavailableException("ServiceUnavailable")))

        when(mockHttpClient.get(any[URL]())(any)).thenReturn(requestBuilder)

        assertThrows[ServiceUnavailableException](await(service.getEmail))
      }
    }
  }

  trait Setup {
    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]

    val application: Application = applicationBuilder
      .overrides(
        inject.bind[MetricsReporterService].toInstance(mockMetricsReporterService),
        inject.bind[HttpClientV2].toInstance(mockHttpClient),
        inject.bind[RequestBuilder].toInstance(requestBuilder)
      )
      .build()

    val service: DataStoreService = application.injector.instanceOf[DataStoreService]

    when(mockMetricsReporterService.withResponseTimeLogging[Email](any)(any)(any))
      .thenAnswer { (i: InvocationOnMock) =>
        i.getArgument[Future[Email]](1)
      }
  }
}
