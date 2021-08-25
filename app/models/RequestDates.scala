/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class RequestDates(dateFrom: LocalDate, dateTo: LocalDate)

object RequestDates {
  implicit val requestDates: OFormat[RequestDates] = Json.format[RequestDates]
}