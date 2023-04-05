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

package helpers

import play.api.i18n.Messages
import services.DateTimeService

import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}
import java.util.Locale


trait DateFormatters {

  def dateAsMonth(date: LocalDate)(implicit messages: Messages): String = messages(s"month.${date.getMonthValue}")
  def dateAsDayMonthAndYear(date: LocalDate)(implicit messages: Messages): String = s"${date.getDayOfMonth} ${dateAsMonth(date)} ${date.getYear}"
  def dateAsMonthAbbr(date: LocalDate)(implicit messages: Messages): String = messages(s"month.abbr.${date.getMonthValue}")
  def dateAsDayMonthAbbrAndYear(date: LocalDate)(implicit messages: Messages): String = s"${date.getDayOfMonth} ${dateAsMonthAbbr(date)} ${date.getYear}"
  def timeAsHourMinutesWithAmPm(dateTime: LocalDateTime): String = DateTimeFormatter.ofPattern("hh:mm a").format(dateTime)
  def updatedDateTime(dateTime: LocalDateTime)(implicit messages: Messages): String = {
    Formatters.timeAsHourMinutesWithAmPm(dateTime).toLowerCase + " "+messages(s"cf.guarantee-account.updated.time.on") + " "+ Formatters.dateAsDayMonthAndYear(dateTime.toLocalDate)
  }
  def dateTimeAsIso8601(dateTime: LocalDateTime): String = {
    s"${DateTimeFormatter.ISO_DATE_TIME.format(dateTime.truncatedTo(ChronoUnit.SECONDS))}Z"
  }
}

trait CurrencyFormatters {
  def formatCurrencyAmount(amount: BigDecimal): String = {
    val maxDecimalPlaces: Int = 2
    val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.UK)
    numberFormat.setMaximumFractionDigits(maxDecimalPlaces)
    numberFormat.setMinimumFractionDigits(maxDecimalPlaces)
    numberFormat.format(amount)
  }

  def formatCurrencyAmount0dp(amount: BigDecimal): String = {
    val numberFormat: NumberFormat =NumberFormat.getCurrencyInstance(Locale.UK)
    val outputDecimals = if (amount.isWhole) 0 else 2
    numberFormat.setMaximumFractionDigits(outputDecimals)
    numberFormat.setMinimumFractionDigits(outputDecimals)
    numberFormat.format(amount)
  }
}


trait FileFormatters {
  def filenameWithDateTime()(implicit messages: Messages, dateTimeService: DateTimeService): String = {
    val formattedTime = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(dateTimeService.localDateTime())
    messages.apply("cf.guarantee-account.csv.filename", formattedTime)
  }

  def filenameWithRequestDates(start: LocalDate, end: LocalDate)(implicit messages: Messages): String = {
    val formatted = DateTimeFormatter.ofPattern("yyyyMM")
    val startDate = formatted.format(start)
    val endDate = formatted.format(end)
    messages.apply("cf.guarantee-account.requested.csv.filename", startDate, endDate)
  }
}

object Formatters extends DateFormatters with CurrencyFormatters with FileFormatters

