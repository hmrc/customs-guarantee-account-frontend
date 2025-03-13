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

import models.*
import uk.gov.hmrc.http.SessionId

import java.time.{LocalDate, LocalDateTime, ZoneOffset}

object TestData {
  val year_2027  = 2027
  val year_2024  = 2024
  val year_2018  = 2018
  val year_2019  = 2019
  val month_12   = 12
  val month_7    = 7
  val day_20     = 20
  val day_26     = 26
  val day_29     = 29
  val hour_12    = 12
  val minutes_30 = 30
  val seconds_55 = 55
  val zero       = 0
  val ten        = 10

  val dayOne         = 1
  val dayTwenty      = 20
  val dayTwentyOne   = 21
  val dayTwentyTwo   = 22
  val dayTwentyThree = 23
  val twoThousand    = 2000
  val eighteen       = 18

  val eori                 = "GB001"
  val sessionId: SessionId = SessionId("session_1234")
  val accountNumber        = "987654"
  val someGan              = "GAN-1"
  val limit                = 123000
  val balance              = 123.45

  val startKey   = "start"
  val endKey     = "end"
  val defaultKey = "default"

  val date: LocalDate          = LocalDate.now()
  val dateTime: LocalDateTime  = LocalDateTime.now()
  val dateInMilliSeconds: Long = dateTime.atZone(ZoneOffset.UTC).toInstant.toEpochMilli
  val dollar                   = "$"
  val fromDate: LocalDate      = LocalDate.parse("2020-10-20")
  val toDate: LocalDate        = LocalDate.parse("2020-12-22")

  val localDateTime: LocalDateTime = LocalDateTime.of(year_2024, month_7, day_26, hour_12, ten, seconds_55)
  val localDate: LocalDate         = LocalDate.of(year_2024, month_7, day_29)

  val amts: Amounts     = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
  val tt: TaxType       = TaxType("VAT", amts)
  val ttg: TaxTypeGroup = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amts, taxType = tt)

  val dd: DueDate =
    DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amts, taxTypeGroups = Seq(ttg))

  val encryptedValue     = "sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI="
  val nonceValue: String = "RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3" +
    "fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"
}
