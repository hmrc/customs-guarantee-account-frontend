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

package controllers

import connectors.{AccountStatusOpen, CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested, UnknownException}
import models._
import play.api.http.Status
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditingService
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.SpecBase

import java.time.{LocalDate, LocalDateTime, Month}
import scala.concurrent.Future

class DownloadCsvControllerSpec extends SpecBase {

  "downloadCsv" should {

    "retrieve open guarantee transactions detail from the API" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Right(ganTransactions.reverse)))

      override val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .configure(
          "application.guarantee-account.numberOfItemsPerPage" -> "10")
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None, None).url)
        val result = route(app, request).value
        contentAsString(result) must include(ganTransactions.head.movementReferenceNumber)
        contentAsString(result) must include(""","MRN-1",""")
        contentAsString(result) must include(""","MRN-2",""")
        contentAsString(result).split("\n").head must startWith(""""Date","MRN","UCR",""")
        contentAsString(result).split("\n").last must startWith(
          """"Guarantee account guidance, including an explanation of tax codes, can be found at https://www.gov.uk""")
      }
    }

    "return OK" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Right(ganTransactions)))

      when(mockAuditingService.auditCsvDownload(any, any, any, any)(any, any)).thenReturn(Future.successful(AuditResult.Success))

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None, None).url)
        val result = route(app, request).value
        status(result) must be(Status.OK)
      }
    }

    "return content disposition of 'attachment' by default" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Right(ganTransactions)))

      when(mockAuditingService.auditCsvDownload(any, any, any, any)(any, any)).thenReturn(Future.successful(AuditResult.Success))

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None, None).url)
        val result = route(app, request).value
        val actualHeaders = headers(result)
        actualHeaders("Content-Disposition") must startWith("attachment")
      }
    }

    "return any content disposition passed in (via query parameter)" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Right(ganTransactions)))

      when(mockAuditingService.auditCsvDownload(any, any, any, any)(any, any)).thenReturn(Future.successful(AuditResult.Success))

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(disposition = Some("inline"), None).url)
        val result = route(app, request).value
        val actualHeaders = headers(result)
        actualHeaders("Content-Disposition") must startWith("inline")
      }
    }

    "redirect to download csv error when the api returns not found" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None, None).url)
        val result = route(app, request).value
        redirectLocation(result).value mustEqual routes.DownloadCsvController.showUnableToDownloadCSV(None).url
      }
    }

    "redirect to download csv error when the api returns 413" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None, None).url)
        val result = route(app, request).value
        redirectLocation(result).value mustEqual routes.DownloadCsvController.showUnableToDownloadCSV(None).url
      }
    }

    "redirect to download csv error when failed to download csv" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None, None).url)
        val result = route(app, request).value
        redirectLocation(result).value mustEqual routes.DownloadCsvController.showUnableToDownloadCSV(None).url
      }
    }

    "return 404" when {
      "the user does not have a guarantee account" in new Setup {
        user =>
        when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(None))

        running(app) {
          val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None, None).url)
          val result = route(app, request).value
          status(result) must be(Status.NOT_FOUND)
        }
      }
    }

    "generate the expected filename" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Right(ganTransactions)))

      override val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .configure(
          "application.guarantee-account.numberOfItemsPerPage" -> "10",
          "features.fixed-systemdate-for-tests" -> true)
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None, None).url)
        val result = route(app, request).value
        val actualHeaders = headers(result)
        actualHeaders("Content-Disposition") must include(s"filename=Open_Guarantees_20271220123000.CSV")
      }
    }

    "throw an exception (to serve up the default error page)" when {
      "AuditingService returns a Failure result" in new Setup {
        when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(Some(guaranteeAccount)))

        when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
          .thenReturn(Future.successful(Right(ganTransactions)))

        when(mockAuditingService.auditCsvDownload(any, any, any, any)(any, any)).thenReturn(Future.successful(AuditResult.Success))

        running(app) {
          val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None, None).url)
          val result = route(app, request).value
          status(result) mustEqual OK
        }

      }
    }

  }

  "downloadRequestedCSV" must {

    "return OK" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Right(ganTransactions)))

      when(mockAuditingService.auditCsvDownload(any, any, any, any)(any, any)).thenReturn(Future.successful(AuditResult.Success))

      running(appRequested) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(None, "2019-10-10", "2019-10-30", None).url)
        val result = route(appRequested, request).value
        status(result) mustEqual OK
      }

    }

    "return Bad request when invalid dates are submitted" in new Setup {

      val request = fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(None, "20-10-10", "2019-10-10", None).url)

      running(appRequested) {
        val result = route(appRequested, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return No Transactions view when no data is returned for the search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

      val request = fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(None, "2019-10-10", "2019-10-10", None).url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")
      running(appRequested) {
        val result = route(appRequested, request).value
        status(result) mustBe OK
        contentAsString(result) must include regex "No guarantee account securities"
      }
    }

    "return Exceeded Threshold view when too many results returned for the search" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      val request = fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(None, "2019-10-10", "2020-10-31", None).url)

      running(appRequested) {
        val result = route(appRequested, request).value
        contentAsString(result) must include regex "Your search returned too many results"
      }
    }


    "return redirect to unable to download csv search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      val request = fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(None, "2019-10-10", "2020-10-31", None).url)

      running(appRequested) {
        val result = route(appRequested, request).value
        status(result) mustBe SEE_OTHER
      }
    }

    "return redirect to unable to download csv when exception is thrown" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      val request = fakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(None, "2019-10-10", "2020-10-31", None).url)

      running(appRequested) {
        val result = route(appRequested, request).value
        status(result) mustBe SEE_OTHER
      }
    }
    "return redirect to unable to download csv when an exception is thrown" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(None, fromDate.toString, toDate.toString, None).url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
      }
    }

    "redirect to download csv error when failed to download a requested csv" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(None, fromDate.toString, toDate.toString, None).url)
        val result = route(app, request).value
        redirectLocation(result).value mustEqual routes.DownloadCsvController.showUnableToDownloadCSV(None).url
      }
    }

    "return 404" when {
      "the user does not have a guarantee account and requests statements" in new Setup {
        user =>
        when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
          .thenReturn(Future.successful(None))

        running(app) {
          val request = FakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(None, fromDate.toString, toDate.toString, None).url)
          val result = route(app, request).value
          status(result) must be(Status.NOT_FOUND)
        }
      }
    }
  }

  "audit the download of the CSV" in new Setup {
    when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(guaranteeAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
      .thenReturn(Future.successful(Right(ganTransactions)))

    when(mockAuditingService.auditCsvDownload(any, any, any, any)(any, any)).thenReturn(Future.successful(AuditResult.Success))

    running(app) {
      val request = FakeRequest(GET, routes.DownloadCsvController.downloadCsv(None, None).url)
      val result = route(app, request).value
      await(result)

      verify(mockAuditingService).auditCsvDownload(eqTo(eori), eqTo(someGan), eqTo(LocalDateTime.parse("2027-12-20T12:30:00")), eqTo(None))(any, any)
    }
  }

  "showUnableToDownloadCSV" should {
    "return OK" in new Setup {

      running(app) {
        val request = FakeRequest(GET, routes.DownloadCsvController.showUnableToDownloadCSV(None).url)
        val result = route(app, request).value
        status(result) must be(Status.OK)
      }
    }
  }


  "audit the download of a requested CSV" in new Setup {
    when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(guaranteeAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, eqTo(fromDate), eqTo(toDate))(any))
      .thenReturn(Future.successful(Right(ganTransactions)))

    when(mockAuditingService.auditCsvDownload(any, any, any, any)(any, any)).thenReturn(Future.successful(AuditResult.Success))

    running(app) {
      val request = FakeRequest(GET, routes.DownloadCsvController.downloadRequestedCsv(None, fromDate.toString, toDate.toString, None).url)
      val result = route(app, request).value
      await(result)

      verify(mockAuditingService).auditCsvDownload(eqTo(eori), eqTo(someGan), eqTo(LocalDateTime.parse("2027-12-20T12:30:00")), eqTo(Some(RequestDates(fromDate, toDate))))(any, any)
    }
  }

  trait Setup {
    val eori = "GB001"
    val someGan = "GAN-1"

    val fromDate = LocalDate.parse("2020-10-20")
    val toDate = LocalDate.parse("2020-12-22")

    val guaranteeAccount = GuaranteeAccount(someGan, eori, AccountStatusOpen, Some(GeneralGuaranteeBalance(
      BigDecimal(123000),
      BigDecimal(123.45)
    )))

    val amt = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
    val tt = TaxType("VAT", amt)
    val ttg = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt)
    val dd = DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg))

    val ganTransactions = List(
      GuaranteeTransaction(LocalDate.of(2018, Month.JULY, 23),
        "MRN-1",
        None,
        BigDecimal(45367.12),
        Some("UCR-1"),
        "GB10000",
        "GB20000",
        BigDecimal(21.00),
        BigDecimal(11.50),
        None,
        None,
        dueDates = Seq(dd)),

      GuaranteeTransaction(LocalDate.of(2018, Month.JULY, 22),
        "MRN-2",
        None,
        BigDecimal(12367.50),
        None,
        "GB30000",
        "GB40000",
        BigDecimal(31.00),
        BigDecimal(25.20),
        None,
        None,
        dueDates = Seq(dd)),

      GuaranteeTransaction(LocalDate.of(2018, Month.JULY, 22),
        "MRN-2",
        None,
        BigDecimal(12368.50),
        None,
        "GB30000",
        "GB40000",
        BigDecimal(33.00),
        BigDecimal(27.20),
        None,
        Some("C18-1"),
        dueDates = Seq(dd)),

      GuaranteeTransaction(LocalDate.of(2018, Month.JULY, 22),
        "MRN-2",
        None,
        BigDecimal(12369.50),
        None,
        "GB30000",
        "GB40000",
        BigDecimal(32.00),
        BigDecimal(26.20),
        None,
        Some("C18-2"),
        dueDates = Seq(dd))
    )

    val mockCustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockAuditingService: AuditingService = mock[AuditingService]

    val app = application
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[AuditingService].toInstance(mockAuditingService))
      .configure(
        "application.guarantee-account.numberOfItemsPerPage" -> "10",
        "features.fixed-systemdate-for-tests" -> true)
      .build()

    lazy val appRequested = application
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[AuditingService].toInstance(mockAuditingService)
      )
      .configure(
        "features.fixed-systemdate-for-tests" -> true)
      .build()

    val nonFatalResponse = UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)
  }

}
