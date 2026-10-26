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

import models.GuaranteeTransactionDates
import play.api.test.Helpers.*
import utils.SpecBase

import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.Future
import utils.Utils.emptyString
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.jsoup.Jsoup
import play.api.Application
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import utils.TestData.{day_20, eori, month_12, month_7, year_2019}

class RequestTransactionControllerSpec extends SpecBase {

  "onPageLoad" should {
    "return OK with pre-populated data" in new Setup {
      when(mockRequestedTransactionsCache.get(eqTo(eori))).thenReturn(Future.successful(Some(transactionDates)))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.RequestTransactionsController.onPageLoad().url)

      running(application) {
        val result = route(application, request).value

        status(result) mustBe OK
        verify(mockRequestedTransactionsCache).get(eqTo(eori))
      }
    }

    "return OK with no cached data" in new Setup {
      when(mockRequestedTransactionsCache.get(eqTo(eori))).thenReturn(Future.successful(None))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.RequestTransactionsController.onPageLoad().url)

      running(application) {
        val result = route(application, request).value

        status(result) mustBe OK
        verify(mockRequestedTransactionsCache).get(eqTo(eori))
      }
    }

    "return OK when DB get throws exception" in new Setup {
      when(mockRequestedTransactionsCache.get(eqTo(eori))).thenReturn(Future.failed(new Exception()))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.RequestTransactionsController.onPageLoad().url)

      running(application) {
        val test = route(application, request).value

        status(test) mustBe OK
        verify(mockRequestedTransactionsCache).get(eqTo(eori))
      }
    }
  }

  "onPageLoad with a fixed system date" should {
    "display the CY-6 inset text and hint examples from one year ago" in new Setup {
      when(mockRequestedTransactionsCache.get(eqTo(eori))).thenReturn(Future.successful(None))

      val app: Application = applicationWithFixedDate("2026-09-25T00:00:00Z")

      running(app) {
        val result = route(app, fakeRequest(GET, routes.RequestTransactionsController.onPageLoad().url)).value
        status(result) mustBe OK

        val body: String = contentAsString(result)
        val doc          = Jsoup.parse(body)

        doc.getElementById("earliest-requestable-year").text() mustBe
          messages("cf.form.error.date-too-far-in-past", "2020")
        doc.getElementById("start-hint").text() mustBe
          messages("cf.guarantee-account.transactions.request.startDate.hint", "9", "2025")
        doc.getElementById("end-hint").text() mustBe
          messages("cf.guarantee-account.transactions.request.endDate.hint", "9", "2025")
        body must not include "October 2019"
      }
    }

    "recalculate the earliest requestable year across the 6 April tax year boundary" in new Setup {
      when(mockRequestedTransactionsCache.get(eqTo(eori))).thenReturn(Future.successful(None))

      Seq(
        "2026-03-31T00:00:00Z" -> "2019",
        "2026-04-05T00:00:00Z" -> "2019",
        "2026-04-06T00:00:00Z" -> "2020",
        "2027-04-05T00:00:00Z" -> "2020",
        "2027-04-06T00:00:00Z" -> "2021"
      ).foreach { case (systemDate, expectedYear) =>
        val app: Application = applicationWithFixedDate(systemDate)

        running(app) {
          val result = route(app, fakeRequest(GET, routes.RequestTransactionsController.onPageLoad().url)).value
          status(result) mustBe OK

          Jsoup.parse(contentAsString(result)).getElementById("earliest-requestable-year").text() mustBe
            messages("cf.form.error.date-too-far-in-past", expectedYear)
        }
      }
    }
  }

  "onSubmit" should {
    "return BAD_REQUEST with the CY-6 error only on the start date when only the start date is before April of CY-6" in new Setup {
      val app: Application = applicationWithFixedDate("2026-09-25T00:00:00Z")

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "3",
            "start.year"  -> "2020",
            "end.month"   -> "5",
            "end.year"    -> "2020"
          )

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST

        val body: String = contentAsString(result)
        val doc          = Jsoup.parse(body)

        doc.getElementById("start-error").text() must include(
          messages("cf.form.error.start.date-too-far-in-past", "2020")
        )
        Option(doc.getElementById("end-error")) mustBe None
        body                                     must not include "October 2019"
      }
    }

    "return BAD_REQUEST with the CY-6 error on both dates when both are before April of CY-6" in new Setup {
      val app: Application = applicationWithFixedDate("2026-09-25T00:00:00Z")

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "8",
            "start.year"  -> "2019",
            "end.month"   -> "9",
            "end.year"    -> "2019"
          )

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST

        val doc = Jsoup.parse(contentAsString(result))

        doc.getElementById("start-error").text() must include(
          messages("cf.form.error.start.date-too-far-in-past", "2020")
        )
        doc.getElementById("end-error").text()   must include(
          messages("cf.form.error.end.date-too-far-in-past", "2020")
        )
      }
    }

    "redirect when the start date is April of the CY-6 year" in new Setup {
      when(mockRequestedTransactionsCache.set(any, any)).thenReturn(Future.successful(true))

      val app: Application = applicationWithFixedDate("2026-09-25T00:00:00Z")

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "4",
            "start.year"  -> "2020",
            "end.month"   -> "5",
            "end.year"    -> "2020"
          )

      running(app) {
        status(route(app, request).value) mustBe SEE_OTHER
      }
    }

    "redirect to requested page when valid data has been submitted" in new Setup {
      when(mockRequestedTransactionsCache.set(any, any))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "10",
            "start.year"  -> "2024",
            "end.month"   -> "11",
            "end.year"    -> "2025"
          )

      running(application) {
        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.RequestedTransactionsController.onPageLoad().url
      }
    }

    "return BAD_REQUEST when the start date is before April of CY-6" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "9",
            "start.year"  -> "2019",
            "end.month"   -> "10",
            "end.year"    -> "2019"
          )

      running(application) {
        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the end date is before April of CY-6" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "10",
            "start.year"  -> "2019",
            "end.month"   -> "9",
            "end.year"    -> "2019"
          )

      running(application) {
        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is future date" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "10",
            "start.year"  -> "2021",
            "end.month"   -> "9",
            "end.year"    -> "2019"
          )

      running(application) {
        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the end date is future date" in new Setup {
      val year: String = LocalDate.now().plusYears(2).getYear.toString

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "10",
            "start.year"  -> "2019",
            "end.month"   -> "10",
            "end.year"    -> year
          )

      running(application) {
        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is after the end date" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "11",
            "start.year"  -> "2019",
            "end.month"   -> "10",
            "end.year"    -> "2019"
          )

      running(application) {
        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the requested data exceeds 6 years in the past" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "10",
            "start.year"  -> "2000",
            "end.month"   -> "10",
            "end.year"    -> "2000"
          )

      running(application) {
        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when invalid data submitted" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.invalid" -> "10",
            "start.year"    -> "2019",
            "end.month"     -> "10",
            "end.year"      -> "2019"
          )

      running(application) {
        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when start date and end date are empty" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> emptyString,
            "start.year"  -> "2019",
            "end.month"   -> emptyString,
            "end.year"    -> "2019"
          )

      running(application) {
        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  trait Setup {
    val application: Application = applicationBuilder
      .configure("features.fixed-systemdate-for-tests" -> "true")
      .build()

    def applicationWithFixedDate(isoInstant: String): Application = applicationBuilder
      .overrides(bind[Clock].toInstance(Clock.fixed(Instant.parse(isoInstant), ZoneId.of("UTC"))))
      .build()
  }

  val transactionDates: GuaranteeTransactionDates = GuaranteeTransactionDates(
    start = LocalDate.of(year_2019, month_7, day_20),
    end = LocalDate.of(year_2019, month_12, day_20)
  )
}
