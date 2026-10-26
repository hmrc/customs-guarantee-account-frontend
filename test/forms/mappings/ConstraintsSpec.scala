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

import org.scalatest.matchers.should.Matchers.*
import play.api.data.validation.{Invalid, Valid, ValidationError, ValidationResult}
import utils.SpecBase
import utils.TestData.*

import java.time.*

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
      "return Invalid taxYearErrorKey with the CY-6 year" in new Setup {
        val sevenYearsAgo            = LocalDate.now(futureClock).minusYears(seven)
        val result: ValidationResult = checkDates(taxYearErrorKey)(futureClock)
          .apply(sevenYearsAgo)

        result mustBe Invalid(List(ValidationError(List(taxYearErrorKey), "2021")))
      }

      "return Invalid taxYearErrorKey for a date before April of the CY-6 year" in new Setup {
        val result: ValidationResult = checkDates(taxYearErrorKey)(clock)
          .apply(LocalDate.of(year_2019, month_3, dayOne))

        result mustBe Invalid(List(ValidationError(List(taxYearErrorKey), "2019")))
      }

      "return Valid for April of the CY-6 year" in new Setup {
        val result: ValidationResult = checkDates(taxYearErrorKey)(clock)
          .apply(LocalDate.of(year_2019, month_4, dayOne))

        result mustBe Valid
      }

      "recalculate the CY-6 year across the 6 April tax year boundary" in new Setup {
        val march2019: LocalDate = LocalDate.of(year_2019, month_3, dayOne)

        Seq(
          "2026-03-31T00:00:00Z" -> "2019",
          "2026-04-05T00:00:00Z" -> "2019",
          "2026-04-06T00:00:00Z" -> "2020",
          "2027-04-05T00:00:00Z" -> "2020",
          "2027-04-06T00:00:00Z" -> "2021"
        ).foreach { case (systemDate, expectedYear) =>
          val fixedClock: Clock = Clock.fixed(Instant.parse(systemDate), ZoneId.of("UTC"))

          checkDates(taxYearErrorKey)(fixedClock).apply(march2019) mustBe
            Invalid(List(ValidationError(List(taxYearErrorKey), expectedYear)))
        }
      }
    }
  }

  trait Setup {
    def ld: LocalDate = LocalDateTime.now().toLocalDate

    val taxYearErrorKey: String = "cf.form.error.start.date-too-far-in-past"

    val month_3 = 3
    val month_4 = 4

    private val fixedInstant = Instant.parse("2025-06-26T17:40:00Z")
    private val zone         = ZoneId.of("UTC")

    implicit val clock: Clock       = Clock.fixed(fixedInstant, zone)
    implicit val futureClock: Clock = Clock.offset(clock, Duration.ofDays(twoYearsInDays))
  }
}
