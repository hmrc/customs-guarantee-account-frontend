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

package forms.mappings

import java.time.{Clock, LocalDate, LocalDateTime}
import org.scalatest.matchers.should.Matchers.*
import play.api.data.validation.{Invalid, Valid, ValidationError, ValidationResult}
import utils.SpecBase
import utils.TestData.{dayOne, eighteen, month_7, twoThousand, year_2019}

class ConstraintsSpec extends SpecBase with Constraints {

  "Constraints" should {
    "equalToOrBeforeToday" must {
      "valid if equal to or before today" in new Setup {
        val result: ValidationResult = equalToOrBeforeToday("error.date").apply(ld)

        result mustBe Valid
      }

      "invalid if date is in future" in new Setup {
        val result: ValidationResult = equalToOrBeforeToday("error.date").apply(ld.plusYears(1))

        result mustBe Invalid(List(ValidationError(List("error.date"))))
      }
    }

    "checkDates" must {
      "return Invalid taxYearErrorKey" in new Setup {
        val result: ValidationResult = checkDates(systemStartDateErrorKey, taxYearErrorKey)(clock)
          .apply(LocalDate.of(twoThousand, month_7, dayOne))

        result mustBe Invalid(List(ValidationError(List(taxYearErrorKey))))
      }

      "return Invalid constraint for year before 2019" in new Setup {
        val result: ValidationResult = checkDates(systemStartDateErrorKey, taxYearErrorKey)(clock)
          .apply(LocalDate.of(year_2019, month_7, dayOne))

        result mustBe Invalid(List(ValidationError(List(systemStartDateErrorKey))))
      }
    }
  }

  trait Setup {
    def ld: LocalDate = LocalDateTime.now().toLocalDate

    val systemStartDateErrorKey: String = "You cannot enter a date before March 2019"
    val taxYearErrorKey: String         = "The from date cannot be older than 6 years from now"

    implicit val clock: Clock = Clock.systemUTC()
  }
}
