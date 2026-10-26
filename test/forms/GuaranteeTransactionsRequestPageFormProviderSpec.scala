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
import utils.TestData.period
import utils.Utils.emptyString

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

    "not apply date range checks when binding" in new Setup {
      val form: Form[GuaranteeTransactionDates] = histDateReqPageForm()

      val formAfterBinding: Form[GuaranteeTransactionDates] = form.bind(
        Map(
          "start.year"  -> s"$year2019",
          "start.month" -> s"$month3",
          "end.year"    -> s"$year2019",
          "end.month"   -> s"$month3"
        )
      )

      formAfterBinding.hasErrors mustBe false
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
    val histDateReqPageForm = new GuaranteeTransactionsRequestPageFormProvider()

    val year2021 = 2021
    val year2019 = 2019
    val month3   = 3
    val month10  = 10
  }
}
