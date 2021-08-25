/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class GuaranteeTransactionDates(start: LocalDate, end: LocalDate)

object GuaranteeTransactionDates {
  implicit val format: OFormat[GuaranteeTransactionDates] = Json.format[GuaranteeTransactionDates]
}
