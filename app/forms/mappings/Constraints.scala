/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package forms.mappings

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.time.TaxYear
import uk.gov.hmrc.time.TaxYear.taxYearFor

import java.time.{Clock, LocalDate, LocalDateTime, Period}

trait Constraints {

  lazy val etmpStatementsDate: LocalDate = LocalDate.of(2019, 10, 1)
  lazy val currentDate: LocalDate = LocalDateTime.now().toLocalDate
  lazy val dayOfMonthThatTaxYearStartsOn = 6

  def equalToOrBeforeToday(errorKey:String): Constraint[LocalDate] = Constraint {
    case request if request.isAfter(currentDate)  =>
      Invalid(ValidationError(errorKey))
    case _ => Valid
  }

  def minTaxYear()(implicit clock:Clock): TaxYear = {
    lazy val currentDate: LocalDate = LocalDateTime.now(clock).toLocalDate
    val maximumNumberOfYears = 6
    taxYearFor(currentDate).back(maximumNumberOfYears)
  }

  def checkDates(systemStartDateErrorKey:String, taxYearErrorKey:String)(implicit clock: Clock): Constraint[LocalDate] = Constraint {
    case request if Period.between(request, etmpStatementsDate).toTotalMonths > 0 =>
      Invalid(ValidationError(systemStartDateErrorKey))
    case request if minTaxYear.starts.isAfter(request.withDayOfMonth(dayOfMonthThatTaxYearStartsOn))  =>
      Invalid(ValidationError(taxYearErrorKey))
    case _ => Valid
  }
}
