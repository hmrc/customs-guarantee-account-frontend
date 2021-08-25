/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import org.joda.time

import java.time.{LocalDate, ZoneId, ZoneOffset}
import java.util.Date

object DateConverters {
  implicit def toLocalDate(date: Date): LocalDate = date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate

  implicit class OrderedLocalDate(date: LocalDate) extends Ordered[LocalDate] {
    def compare(that: LocalDate): Int = date.compareTo(that)
  }

  implicit def toJodaTime(date: LocalDate): time.LocalDate  = {
    new time.LocalDate(java.util.Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC)))
  }
}
