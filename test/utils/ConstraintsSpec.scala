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

package utils

import java.time.{Clock, LocalDate, LocalDateTime}
import org.scalatest.matchers.should.Matchers._
import forms.mappings.Constraints
import play.api.data.validation.{Invalid, Valid, ValidationError}

//TODO: To be moved to forms.mappings
class ConstraintsSpec extends SpecBase with Constraints {

  "Constraints" should {

    "equalToOrBeforeToday" must {
      "valid if equal to or before today" in new Setup {
        val result = equalToOrBeforeToday("error.date").apply(ld)

        result mustBe Valid
      }

      "invalid if date is in future" in new Setup {
        val result = equalToOrBeforeToday("error.date").apply(ld.plusYears(1))

        result mustBe Invalid(List(ValidationError(List("error.date"))))
      }

    }

    "checkDates" must {
      "return Invalid year error" in new Setup {
        val result = checkDates(systemStartDateErrorKey, taxYearErrorKey, yearLengthError)(clock)
          .apply(LocalDate.of(18, 10, 1))

        result mustBe Invalid(List(ValidationError(List(yearLengthError))))
      }

      "return Invalid constraint for year before 2019" in new Setup {
        val result = checkDates(systemStartDateErrorKey, taxYearErrorKey, yearLengthError)(clock)
          .apply(LocalDate.of(2018, 10, 1))

        result mustBe Invalid(List(ValidationError(List(systemStartDateErrorKey))))
      }
    }
  }

  trait Setup {
    def ld: LocalDate = LocalDateTime.now().toLocalDate

    val systemStartDateErrorKey: String = "You cannot enter a date before October 2019"
    val taxYearErrorKey: String = "The from date cannot be older than 6 years from now"
    val yearLengthError: String = "Year must include 4 numbers"

    implicit val clock: Clock = Clock.systemUTC()
  }
}
