/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import connectors.{AccountStatusOpen, CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested, UnknownException}
import models._
import play.api.http.Status
import play.api.inject.bind
import play.api.test.Helpers._
import services.{AuditingService, DateTimeService}
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase

import java.time.{LocalDate, Month}
import scala.concurrent.Future

class GuaranteeTransactionsRequestPageControllerSpec extends SpecBase {

  "onPageLoad" should {
    "return OK " in new Setup {
      val request = fakeRequest(GET, routes.GuaranteeTransactionsRequestPageController.onPageLoad().url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

  }

  "onSubmit" should {

    "return status Ok when valid data has been submitted" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Right(ganTransactions)))


      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return No Transactions view when no data is returned for the search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) must include regex "No guarantee account securities"
      }
    }

    "return Exceeded Threshold view when too many results returned for the search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) must include regex "Your search returned too many results"
      }
    }

    "return transaction unavailable for internal server error during search" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) must include regex "We are unable to show your live guarantees at the moment. Please try again later."
      }
    }

    "redirect to account unavailable page when exception is thrown" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any))
        .thenThrow(new RuntimeException())

      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")
      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
      }
    }

    "return BAD_REQUEST when the start date is earlier than system start date" in new Setup {
      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "9", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the end date is earlier than system start date" in new Setup {
      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "9", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is future date" in new Setup {
      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2021", "end.month" -> "9", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the end date is future date" in new Setup {
      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2021")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is after the end date" in new Setup {
      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "11", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the requested data exceeds 6 years in the past" in new Setup {
      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2000", "end.month" -> "10", "end.year" -> "2000")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when invalid data submitted" in new Setup {
      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.invalid" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when start date and end date are empty" in new Setup {
      val request = fakeRequest(POST, routes.GuaranteeTransactionsRequestPageController.onSubmit().url)
        .withFormUrlEncodedBody("start.month" -> "", "start.year" -> "2019", "end.month" -> "", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  trait Setup {

    val eori = "GB001"
    val someGan = "GAN-1"

    val mockDateTimeService = mock[DateTimeService]
    val mockCustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockAuditingService: AuditingService = mock[AuditingService]

    val guaranteeAccount = GuaranteeAccount(someGan, eori, AccountStatusOpen, Some(GeneralGuaranteeBalance(
      BigDecimal(123000),
      BigDecimal(123.45)
    )))

    val amt = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
    val tt = TaxType("VAT", amt)
    val ttg = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt)
    val dd = DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg))

    val ganTransactions = List(
      GuaranteeTransaction(LocalDate.of(2019, Month.OCTOBER, 23),
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

      GuaranteeTransaction(LocalDate.of(2019, Month.OCTOBER, 22),
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

      GuaranteeTransaction(LocalDate.of(2019, Month.OCTOBER, 22),
        "MRN-3",
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

      GuaranteeTransaction(LocalDate.of(2019, Month.OCTOBER, 22),
        "MRN-4",
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
    val nonFatalResponse = UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)
    val app = application
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
        bind[AuditingService].toInstance(mockAuditingService)
      )
      .configure(
        "features.fixed-systemdate-for-tests" -> "true")
      .build()
  }
  }
