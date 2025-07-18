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

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.time.TaxYear
import uk.gov.hmrc.time.TaxYear.taxYearFor

import java.time.{Clock, LocalDate, LocalDateTime, Period}

trait Constraints {

  val year  = 2019
  val month = 10
  val day   = 1
  val zero  = 0

  private lazy val etmpStatementsDate: LocalDate = LocalDate.of(year, month, day)
  private lazy val currentDate: LocalDate        = LocalDateTime.now().toLocalDate
  private lazy val dayOfMonthThatTaxYearStartsOn = 6

  def equalToOrBeforeToday(errorKey: String): Constraint[LocalDate] = Constraint {

    case request if request.isAfter(currentDate) =>
      Invalid(ValidationError(errorKey))

    case _ => Valid
  }

  private def minTaxYear()(implicit clock: Clock): TaxYear = {
    lazy val currentDate: LocalDate = LocalDateTime.now(clock).toLocalDate
    val maximumNumberOfYears        = 6

    taxYearFor(currentDate).back(maximumNumberOfYears)
  }

  def checkDates(systemStartDateErrorKey: String, taxYearErrorKey: String)(implicit
    clock: Clock
  ): Constraint[LocalDate] = Constraint {

    case request if Period.between(request, etmpStatementsDate).toTotalMonths > zero =>
      Invalid(ValidationError(systemStartDateErrorKey))

    case request if minTaxYear().starts.isAfter(request.withDayOfMonth(dayOfMonthThatTaxYearStartsOn)) =>
      Invalid(ValidationError(taxYearErrorKey))

    case _ => Valid
  }
}
