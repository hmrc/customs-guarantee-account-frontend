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

import connectors._
import models._
import play.api.http.Status
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase

import java.time._
import scala.concurrent.Future

class RequestedTransactionsControllerSpec extends SpecBase {

  "redirect to request page if no requested dates found in cache" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(None))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.RequestTransactionsController.onPageLoad().url
    }
  }

  "return No Transactions view when no data is returned for the search" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(GuaranteeTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(guaranteeAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(
      eqTo(someGan), any, any, any)(any)).thenReturn(Future.successful(Left(NoTransactionsAvailable)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe OK
      contentAsString(result) must include regex "No guarantee account securities"
    }
  }

  "return Exceeded Threshold view when too many results returned for the search" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(GuaranteeTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(guaranteeAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(
      eqTo(someGan), any, any, any)(any)).thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe OK
      contentAsString(result) must include regex "Your search returned too many results"
    }
  }

  "return transaction unavailable for internal server error during search" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(GuaranteeTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(guaranteeAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(
      eqTo(someGan), any, any, any)(any)).thenReturn(Future.successful(Left(UnknownException)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe OK

      contentAsString(result) must include regex
        "We are unable to show your live guarantees at the moment. Please try again later."
    }
  }

  "redirect to account unavailable page when exception is thrown" in new Setup {

    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(GuaranteeTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(guaranteeAccount)))

    when(mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(
      eqTo(someGan), any, any, any)(any)).thenThrow(new RuntimeException())

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(app) {
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
    }
  }

  trait Setup {

    val eori = "GB001"
    val someGan = "GAN-1"
    val mockCustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]

    val guaranteeAccount = GuaranteeAccount(
      someGan, eori, AccountStatusOpen, Some(GeneralGuaranteeBalance(
        BigDecimal(123000),
        BigDecimal(123.45)
    )))

    val amt = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
    val tt = TaxType("VAT", amt)
    val ttg = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt)

    val dd = DueDate(
      dueDate = "2020-07-28",
      reasonForSecurity = Some("T24"),
      amounts = amt, taxTypeGroups = Seq(ttg))

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

    val nonFatalResponse = UpstreamErrorResponse("ServiceUnavailable",
      Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)

    val app = application.overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector)
      ).configure("features.fixed-systemdate-for-tests" -> "true").build()
  }
}
