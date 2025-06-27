/*
 * Copyright 2025 HM Revenue & Customs
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

package forms

import models.GuaranteeTransactionDates
import play.api.data.{Form, FormError}
import utils.SpecBase
import utils.TestData.{period, seven, twoYearsInDays}
import utils.Utils.emptyString

import java.time.{Clock, Duration, Instant, YearMonth, ZoneId}

class GuaranteeTransactionsRequestPageFormProviderSpec extends SpecBase {

  "apply" should {

    "bind the form correctly with correct date" in new Setup {
      val form: Form[GuaranteeTransactionDates] = histDateReqPageForm()

      val formAfterBinding: Form[GuaranteeTransactionDates] = form.bind(
        Map(
          "start.year"  -> s"$year2021",
          "start.month" -> s"$month3",
          "end.year"    -> s"$year2021",
          "end.month"   -> s"$month10"
        )
      )

      formAfterBinding.hasErrors mustBe false
    }

    "throw error when start date is before 2019-10-1" in new Setup {
      val form: Form[GuaranteeTransactionDates] = histDateReqPageForm()

      val formAfterBinding: Form[GuaranteeTransactionDates] = form.bind(
        Map(
          "start.year"  -> s"$year2019",
          "start.month" -> s"$month3",
          "end.year"    -> s"$year2019",
          "end.month"   -> s"$month10"
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start",
          List(
            "cf.form.error.startDate.date-earlier-than-system-start-date"
          ),
          List()
        )
      ) mustBe true
    }

    "throw error when start date is more than 6 years ago" in new Setup {
      val form: Form[GuaranteeTransactionDates] = futureHistDateReqPageForm()

      val sevenYearsAgo = YearMonth.now(futureClock).minusYears(seven)

      val formAfterBinding: Form[GuaranteeTransactionDates] = form.bind(
        Map(
          "start.year"  -> s"${sevenYearsAgo.getYear}",
          "start.month" -> s"${sevenYearsAgo.getMonthValue}",
          "end.year"    -> s"$year2024",
          "end.month"   -> s"$month10"
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start",
          List(
            "cf.form.error.start.date-too-far-in-past"
          ),
          List()
        )
      ) mustBe true
    }

    "throw error for empty start and end dates" in new Setup {
      val form: Form[GuaranteeTransactionDates] = histDateReqPageForm()

      val formAfterBinding: Form[GuaranteeTransactionDates] = form.bind(
        Map(
          "start.year"  -> emptyString,
          "start.month" -> emptyString,
          "end.year"    -> emptyString,
          "end.month"   -> emptyString
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start",
          List(
            "cf.form.error.start.date-missing"
          ),
          List()
        )
      ) mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "end",
          List(
            "cf.form.error.end.date-missing"
          ),
          List()
        )
      ) mustBe true

    }

    "return an error for empty year field" in new Setup {
      val form: Form[GuaranteeTransactionDates] = histDateReqPageForm()

      val formAfterBinding: Form[GuaranteeTransactionDates] = form.bind(
        Map(
          "start.year"  -> emptyString,
          "start.month" -> s"$month3",
          "end.year"    -> s"$year2021",
          "end.month"   -> s"$month10"
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start.year",
          List(
            "cf.form.error.start.year.date-number-missing"
          ),
          List()
        )
      ) mustBe true
    }

    "return an error for invalid year field" in new Setup {
      val form: Form[GuaranteeTransactionDates] = histDateReqPageForm()

      val formAfterBinding: Form[GuaranteeTransactionDates] = form.bind(
        Map(
          "start.year"  -> period,
          "start.month" -> s"$month3",
          "end.year"    -> s"$year2021",
          "end.month"   -> s"$month10"
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start.year",
          List(
            "cf.form.error.start.year.invalid"
          ),
          List()
        )
      ) mustBe true
    }

    "return an error for empty month field" in new Setup {
      val form: Form[GuaranteeTransactionDates] = histDateReqPageForm()

      val formAfterBinding: Form[GuaranteeTransactionDates] = form.bind(
        Map(
          "start.year"  -> s"$year2021",
          "start.month" -> emptyString,
          "end.year"    -> s"$year2021",
          "end.month"   -> s"$month10"
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start.month",
          List(
            "cf.form.error.start.month.date-number-missing"
          ),
          List()
        )
      ) mustBe true
    }

    "return an error for invalid month field" in new Setup {
      val form: Form[GuaranteeTransactionDates] = histDateReqPageForm()

      val formAfterBinding: Form[GuaranteeTransactionDates] = form.bind(
        Map(
          "start.year"  -> s"$year2021",
          "start.month" -> period,
          "end.year"    -> s"$year2021",
          "end.month"   -> s"$month10"
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start.month",
          List(
            "cf.form.error.start.month.invalid"
          ),
          List()
        )
      ) mustBe true
    }

    "return an error for both invalid month and year field" in new Setup {
      val form: Form[GuaranteeTransactionDates] = histDateReqPageForm()

      val formAfterBinding: Form[GuaranteeTransactionDates] = form.bind(
        Map(
          "start.year"  -> period,
          "start.month" -> period,
          "end.year"    -> s"$year2021",
          "end.month"   -> s"$month10"
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start",
          List(
            "cf.form.error.start.date.invalid"
          ),
          List()
        )
      ) mustBe true
    }
  }

  trait Setup {
    private val fixedInstant = Instant.parse("2025-06-26T17:40:00Z")
    private val zone         = ZoneId.of("UTC")

    implicit val clock: Clock       = Clock.fixed(fixedInstant, zone)
    implicit val futureClock: Clock = Clock.offset(clock, Duration.ofDays(twoYearsInDays))

    val histDateReqPageForm       = new GuaranteeTransactionsRequestPageFormProvider()(clock)
    val futureHistDateReqPageForm = new GuaranteeTransactionsRequestPageFormProvider()(futureClock)

    val year2021 = 2021
    val year2024 = 2024
    val year2019 = 2019

    val month3  = 3
    val month4  = 4
    val month10 = 10

    val day12 = 12
    val day10 = 10
    val day1  = 1
  }
}
