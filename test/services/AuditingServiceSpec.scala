/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import config.AppConfig
import models.{AuditModel, GuaranteeCsvAuditData}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import utils.SpecBase

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuditingServiceSpec extends SpecBase  {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec = implicitly[ExecutionContext]


  val eori = "GB744638982000"
  val gan = "1234567"
  val fromDate = LocalDate.parse("2020-10-20")
  val toDate = LocalDate.parse("2020-12-22")
  val auditModelWithDates = AuditModel("DownloadGuaranteeStatement", "Download guarantee transactions",
    Json.toJson(GuaranteeCsvAuditData(eori, gan, "open", "2027-12-20T12:30:00" , "CSV", Some(fromDate),Some(toDate))))

  val auditModelWithoutDates = AuditModel("DownloadGuaranteeStatement", "Download guarantee transactions",
    Json.toJson(GuaranteeCsvAuditData(eori, gan, "open", "2027-12-20T12:30:00" , "CSV", None, None)))

  val appName = "customs-guarantee-account-frontend"

  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  when(mockAuditConnector.sendExtendedEvent(any)(any,any)).thenReturn(Future.successful(AuditResult.Success))

  val app = application.overrides(bind[AuditConnector].toInstance(mockAuditConnector)).build()
  val testAuditingService = app.injector.instanceOf[AuditingService]

  "AuditService" should {
    "return success for a valid auditModel with to and from dates " in {
      running(app){
        val result = await(testAuditingService.audit(auditModelWithDates)(hc, ec))
        result must be(AuditResult.Success)
      }
    }

    "return success for a valid auditModel without to and from dates " in {
      running(app){
        val result = await(testAuditingService.audit(auditModelWithoutDates)(hc, ec))
        result must be(AuditResult.Success)
      }

    }

    "return failed for an invalid audit model" in {

      when(mockAuditConnector.sendExtendedEvent(any)(any,any)).thenReturn(Future.successful(AuditResult.Failure("Boom")))

      running(app){
        val result = await(testAuditingService.audit(auditModelWithoutDates)(hc, ec))
        result must be(AuditResult.Failure("Boom"))
      }
    }
  }

}
