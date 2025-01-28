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
import utils.TestData.{balance, dayTwentyThree, dayTwentyTwo, dd, eori, limit, someGan, year_2019}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application

import java.time._
import scala.concurrent.Future

class RequestedTransactionsControllerSpec extends SpecBase {

  "redirect to request page if no requested dates found in cache" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(None))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(application) {
      val result = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.RequestTransactionsController.onPageLoad().url
    }
  }

  "return No Transactions view when no data is returned for the search" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(GuaranteeTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(guaranteeAccount)))

    when(
      mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any)
    ).thenReturn(Future.successful(Left(NoTransactionsAvailable)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(application) {
      val result = route(application, request).value

      status(result) mustBe OK
      contentAsString(result) must include regex "No guarantee account securities"
    }
  }

  "return Exceeded Threshold view when too many results returned for the search" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(GuaranteeTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(guaranteeAccount)))

    when(
      mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any)
    ).thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(application) {
      val result = route(application, request).value

      status(result) mustBe OK
      contentAsString(result) must include regex "Your search returned too many results"
    }
  }

  "return transaction unavailable for internal server error during search" in new Setup {
    when(mockRequestedTransactionsCache.get(any))
      .thenReturn(Future.successful(Some(GuaranteeTransactionDates(LocalDate.now(), LocalDate.now()))))

    when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
      .thenReturn(Future.successful(Some(guaranteeAccount)))

    when(
      mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any)
    ).thenReturn(Future.successful(Left(UnknownException)))

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(application) {
      val result = route(application, request).value
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

    when(
      mockCustomsFinancialsApiConnector.retrieveRequestedGuaranteeTransactionsDetail(eqTo(someGan), any, any, any)(any)
    ).thenThrow(new RuntimeException())

    val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.RequestedTransactionsController.onPageLoad().url)

    running(application) {
      val result = route(application, request).value

      status(result) mustBe SEE_OTHER
    }
  }

  trait Setup {
    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]

    val guaranteeAccount: GuaranteeAccount = GuaranteeAccount(
      someGan,
      eori,
      AccountStatusOpen,
      Some(GeneralGuaranteeBalance(BigDecimal(limit), BigDecimal(balance)))
    )

    val ganTransactions: Seq[GuaranteeTransaction] = List(
      GuaranteeTransaction(
        LocalDate.of(year_2019, Month.OCTOBER, dayTwentyThree),
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
        dueDates = Seq(dd)
      ),
      GuaranteeTransaction(
        LocalDate.of(year_2019, Month.OCTOBER, dayTwentyTwo),
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
        dueDates = Seq(dd)
      ),
      GuaranteeTransaction(
        LocalDate.of(year_2019, Month.OCTOBER, dayTwentyTwo),
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
        dueDates = Seq(dd)
      ),
      GuaranteeTransaction(
        LocalDate.of(year_2019, Month.OCTOBER, dayTwentyTwo),
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
        dueDates = Seq(dd)
      )
    )

    val nonFatalResponse: UpstreamErrorResponse =
      UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)

    val application: Application = applicationBuilder
      .overrides(
        bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector)
      )
      .configure("features.fixed-systemdate-for-tests" -> "true")
      .build()
  }
}
