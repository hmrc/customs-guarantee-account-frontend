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

import play.api.test.Helpers.*
import utils.SpecBase
import java.time.LocalDate
import scala.concurrent.Future
import utils.Utils.emptyString
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest

class RequestTransactionControllerSpec extends SpecBase {

  "onPageLoad" should {
    "return OK with pre-populated data" in new Setup {
      when(mockRequestedTransactionsCache.clear(any)).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.RequestTransactionsController.onPageLoad().url)

      running(application) {
        val result = route(application, request).value

        status(result) mustBe OK
      }
    }

    "return OK with no cached data" in new Setup {
      when(mockRequestedTransactionsCache.clear(any)).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.RequestTransactionsController.onPageLoad().url)

      running(application) {
        val result = route(application, request).value

        status(result) mustBe OK
      }
    }

    "return OK when clearing cache" in new Setup {
      when(mockRequestedTransactionsCache.clear(any)).thenReturn(Future.successful(true))

      val store: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.RequestTransactionsController.onSubmit().url)

      val clear: FakeRequest[AnyContentAsEmpty.type] =
        fakeRequest(GET, routes.RequestTransactionsController.onPageLoad().url)

      running(application) {
        val result = route(application, store).value
        val test   = route(application, clear).value

        status(result) mustBe OK
        status(test) mustBe OK
      }
    }
  }

  "onSubmit" should {
    "redirect to requested page when valid data has been submitted" in new Setup {
      when(mockRequestedTransactionsCache.set(any, any))
        .thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest(POST, routes.RequestTransactionsController.onSubmit().url)
          .withFormUrlEncodedBody(
            "start.month" -> "10",
            "start.year"  -> "2019",
            "end.month"   -> "10",
            "end.year"    -> "2019"
          )

      running(application) {
        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.RequestedTransactionsController.onPageLoad().url
      }
    }

    "return BAD_REQUEST when the start date is earlier than system start date" in new Setup {
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

    "return BAD_REQUEST when the end date is earlier than system start date" in new Setup {
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
  }
}
