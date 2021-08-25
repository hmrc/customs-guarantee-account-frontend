/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import config.AppConfig

import java.time._
import javax.inject.Inject

class DateTimeService @Inject()(appConfig: AppConfig) {

  def getTimeStamp: OffsetDateTime = OffsetDateTime.ofInstant( Instant.now() , ZoneOffset.UTC)

  def systemDateTime(zoneId: ZoneId): LocalDateTime = {

    if (appConfig.fixedDateTime)
      LocalDateTime.of(LocalDate.of(2027, 12, 20), LocalTime.of(12,30)) // scalastyle:ignore
    else
      LocalDateTime.now(zoneId)
  }

  def utcDateTime(): LocalDateTime = systemDateTime(ZoneId.of("UTC"))

  def localDateTime(): LocalDateTime = systemDateTime(ZoneId.of("Europe/London"))

}