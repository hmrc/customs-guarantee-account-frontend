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

import models.UnverifiedEmail
import org.mockito.invocation.InvocationOnMock
import play.api.inject
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, ServiceUnavailableException, UpstreamErrorResponse}
import utils.SpecBase

import scala.concurrent.Future

class DataStoreServiceSpec extends SpecBase {

  "Data store service" should {
    "return existing email" in new Setup {
      val jsonResponse = """{"address":"someemail@mail.com"}""".stripMargin
      when[Future[EmailResponse]](mockHttp.GET(any, any, any)(any, any, any)).thenReturn(
        Future.successful(Json.parse(jsonResponse).as[EmailResponse]))

      running(app) {
        val response = service.getEmail(eori)
        val result = await(response)
        result mustBe Right(Email("someemail@mail.com"))
      }
    }

    "return a UnverifiedEmail on error response" in new Setup {
      when[Future[EmailResponse]](mockHttp.GET(any, any, any)(any, any, any)).thenReturn(
        Future.failed(UpstreamErrorResponse("NoData", 404, 404)))

      running(app) {
        val response = service.getEmail(eori)
        await(response) mustBe Left(UnverifiedEmail)
      }
    }

    "throw service unavailable" in new Setup {
      running(app) {
        val eori = "ETMP500ERROR"
        when[Future[EmailResponse]](mockHttp.GET(any, any, any)(any, any, any)).thenReturn(
          Future.failed(new ServiceUnavailableException("ServiceUnavailable")))
        assertThrows[ServiceUnavailableException](await(service.getEmail(eori)))
      }
    }
  }

  trait Setup {
    val mockMetricsReporterService = mock[MetricsReporterService]
    val mockHttp = mock[HttpClient]
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val eori = "GB11111"

    val app = application.overrides(
      inject.bind[MetricsReporterService].toInstance(mockMetricsReporterService),
      inject.bind[HttpClient].toInstance(mockHttp)
    ).build()

    val service = app.injector.instanceOf[DataStoreService]

    when(mockMetricsReporterService.withResponseTimeLogging[Email](any)(any)(any))
      .thenAnswer((i: InvocationOnMock) => {
        i.getArgument[Future[Email]](1)
      })
  }

}
