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
import models.*
import org.scalatest.matchers.should.Matchers.*
import play.api.inject.bind
import play.api.libs.json.*
import play.api.test.Helpers.*
import uk.gov.hmrc.play.audit.http.connector.*
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.SpecBase
import utils.TestData.{eori, fromDate, toDate}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.Mockito.verify
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application

import java.time.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditingServiceSpec extends SpecBase {

  "AuditService" should {
    "return success for a valid auditModel with to and from dates " in new Setup {
      val expectedDetail: JsValue = Json.toJson(
        GuaranteeCsvAuditData(
          eori,
          guaranteeAccountNumber,
          "open",
          "2027-12-20T12:30:00",
          "CSV",
          Some(fromDate),
          Some(toDate)
        )
      )

      running(application) {
        val result = await(testAuditingService.audit(auditModelWithDates))

        verify(mockAuditConnector).sendExtendedEvent(dataEventCaptor.capture)(any, any)

        val dataEvent = dataEventCaptor.getValue

        dataEvent.auditSource should be("customs-guarantee-account-frontend")
        dataEvent.auditType   should be("DownloadGuaranteeStatement")
        dataEvent.detail      should be(expectedDetail)
        result                  must be(AuditResult.Success)
      }
    }

    "return success for a valid auditModel without to and from dates" in new Setup {
      val expectedDetail: JsValue = Json.toJson(
        GuaranteeCsvAuditData(eori, guaranteeAccountNumber, "open", "2027-12-20T12:30:00", "CSV", None, None)
      )

      running(application) {
        val result = await(testAuditingService.audit(auditModelWithoutDates))

        verify(mockAuditConnector).sendExtendedEvent(dataEventCaptor.capture)(any, any)

        val dataEvent = dataEventCaptor.getValue

        dataEvent.auditSource should be("customs-guarantee-account-frontend")
        dataEvent.auditType   should be("DownloadGuaranteeStatement")
        dataEvent.detail      should be(expectedDetail)
        result                  must be(AuditResult.Success)
      }
    }

    "return failed for an invalid audit model" in new Setup {
      val expectedDetail: JsValue = Json.toJson(
        GuaranteeCsvAuditData(eori, guaranteeAccountNumber, "open", "2027-12-20T12:30:00", "CSV", None, None)
      )

      when(mockAuditConnector.sendExtendedEvent(any)(any, any))
        .thenReturn(Future.successful(AuditResult.Failure("Boom")))

      running(application) {
        val result = await(testAuditingService.audit(auditModelWithoutDates))

        verify(mockAuditConnector).sendExtendedEvent(dataEventCaptor.capture)(any, any)

        val dataEvent = dataEventCaptor.getValue

        dataEvent.auditSource should be("customs-guarantee-account-frontend")
        dataEvent.auditType   should be("DownloadGuaranteeStatement")
        dataEvent.detail      should be(expectedDetail)
        result                  must be(AuditResult.Failure("Boom"))
      }
    }

    "return success for a valid auditModel with to and from dates when downloading a CSV" in new Setup {
      val expectedDetail: JsValue = Json.toJson(
        GuaranteeCsvAuditData(
          eori,
          guaranteeAccountNumber,
          "open",
          "2027-12-20T12:30:00Z",
          "CSV",
          Some(fromDate),
          Some(toDate)
        )
      )

      running(application) {
        val result: Unit = await(
          testAuditingService.auditCsvDownload(
            eori,
            guaranteeAccountNumber,
            LocalDateTime.parse("2027-12-20T12:30:00"),
            Some(RequestDates(fromDate, toDate))
          )
        )

        verify(mockAuditConnector).sendExtendedEvent(dataEventCaptor.capture)(any, any)

        val dataEvent = dataEventCaptor.getValue

        dataEvent.auditSource should be("customs-guarantee-account-frontend")
        dataEvent.auditType   should be("DownloadGuaranteeStatement")
        dataEvent.detail      should be(expectedDetail)
        result                  must be(())
      }
    }

    "return success for a valid auditModel without to and from dates when downloading a CSV" in new Setup {
      val expectedDetail: JsValue = Json.toJson(
        GuaranteeCsvAuditData(eori, guaranteeAccountNumber, "open", "2027-12-20T12:30:00Z", "CSV", None, None)
      )

      running(application) {
        val result: Unit = await(
          testAuditingService
            .auditCsvDownload(eori, guaranteeAccountNumber, LocalDateTime.parse("2027-12-20T12:30:00"), None)
        )

        verify(mockAuditConnector).sendExtendedEvent(dataEventCaptor.capture)(any, any)

        val dataEvent = dataEventCaptor.getValue

        dataEvent.auditSource should be("customs-guarantee-account-frontend")
        dataEvent.auditType   should be("DownloadGuaranteeStatement")
        dataEvent.detail      should be(expectedDetail)
        result                  must be(())
      }
    }

    "log an error message for an invalid audit model when downloading a CSV" in new Setup {
      when(mockAuditConnector.sendExtendedEvent(any)(any, any))
        .thenReturn(Future.successful(AuditResult.Failure("Boom")))

      val expectedDetail: JsValue = Json.toJson(
        GuaranteeCsvAuditData(eori, guaranteeAccountNumber, "open", "2027-12-20T12:30:00Z", "CSV", None, None)
      )

      running(application) {
        val result: Unit = await(
          testAuditingService
            .auditCsvDownload(eori, guaranteeAccountNumber, LocalDateTime.parse("2027-12-20T12:30:00"), None)
        )

        verify(mockAuditConnector).sendExtendedEvent(dataEventCaptor.capture)(any, any)

        val dataEvent = dataEventCaptor.getValue

        dataEvent.auditSource should be("customs-guarantee-account-frontend")
        dataEvent.auditType   should be("DownloadGuaranteeStatement")
        dataEvent.detail      should be(expectedDetail)
        result                  must be(())
      }
    }

    "throw an exception when the send fails to connect when downloading a CSV" in new Setup {
      when(mockAuditConnector.sendExtendedEvent(any)(any, any))
        .thenReturn(Future.failed(new Exception("Failed connection")))

      running(application) {
        intercept[Exception] {
          await(
            testAuditingService
              .auditCsvDownload(eori, guaranteeAccountNumber, LocalDateTime.parse("2027-12-20T12:30:00"), None)
          )
        }
      }
    }
  }

  trait Setup {
    val guaranteeAccountNumber = "1234567"
    val dataEventCaptor        = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

    val auditModelWithDates: AuditModel = AuditModel(
      "DownloadGuaranteeStatement",
      "Download guarantee transactions",
      Json.toJson(
        GuaranteeCsvAuditData(
          eori,
          guaranteeAccountNumber,
          "open",
          "2027-12-20T12:30:00",
          "CSV",
          Some(fromDate),
          Some(toDate)
        )
      )
    )

    val auditModelWithoutDates: AuditModel = AuditModel(
      "DownloadGuaranteeStatement",
      "Download guarantee transactions",
      Json.toJson(GuaranteeCsvAuditData(eori, guaranteeAccountNumber, "open", "2027-12-20T12:30:00", "CSV", None, None))
    )

    val appName = "customs-guarantee-account-frontend"

    val mockAppConfig: AppConfig           = mock[AppConfig]
    val mockAuditConnector: AuditConnector = mock[AuditConnector]

    when(mockAuditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.successful(AuditResult.Success))

    val application: Application = applicationBuilder
      .overrides(bind[AuditConnector].toInstance(mockAuditConnector))
      .build()

    val testAuditingService: AuditingService = application.injector.instanceOf[AuditingService]
  }
}
