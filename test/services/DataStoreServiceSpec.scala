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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import utils.TestData.{day_26, month_7, year_2024}

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

  "EmailResponse.format" should {
    "create the object correctly for Json Reads" in new Setup {
      import EmailResponse.format

      val emailResOb: EmailResponse = Json.parse(sampleEmailResponse).as[EmailResponse]

      emailResOb.address mustBe Some("john.doe@example.com")
    }

    "generate the correct output for Json Writes" in new Setup {
      val emailResponseResultString: String = Json.toJson(emailResponseObject).toString

      assert(emailResponseResultString.contains("john.doe@example.com"))
      assert(emailResponseResultString.contains("email@email.com"))
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

    val sampleEmailResponse: String =
      """{"address":"john.doe@example.com",
        |"imestamp":"2023-12-15T23:25:25.000Z",
        |"undeliverable":{
        |"event":{
        |"event":"someEvent",
        |"emailAddress":"email@email.com",
        |"code":12,
        |"id":"example-id",
        |"detected":"2021-05-14T10:59:45.811+01:00",
        |"enrolment":"HMRC-CUS-ORG~EORINumber~GB744638982004",
        |"reason":"Inbox full"
        |},
        |"subject":"subject-example",
        |"timestamp":"2024-07-26T01:02:00.000Z",
        |"eventId":"example-id",
        |"groupId":"example-group-id"
        |}
        |}""".stripMargin

    val eventCode = 12

    val undelInfoEventOb: UndeliverableInformationEvent = UndeliverableInformationEvent(
      "example-id",
      "someEvent",
      "email@email.com",
      "2021-05-14T10:59:45.811+01:00",
      Some(eventCode),
      Some("Inbox full"),
      "HMRC-CUS-ORG~EORINumber~GB744638982004"
    )

    val undelInfoOb: UndeliverableInformation = UndeliverableInformation(
      "subject-example",
      "example-id",
      "example-group-id",
      DateTime(year_2024, month_7, day_26, 1, 2),
      undelInfoEventOb
    )

    val emailResponseObject: EmailResponse = EmailResponse(
      address = Some("john.doe@example.com"),
      imestamp = Some("2023-12-15T23:25:25.000Z"),
      undeliverable = Some(undelInfoOb)
    )
  }
}
