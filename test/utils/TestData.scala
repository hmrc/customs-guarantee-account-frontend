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

import crypto.EncryptedValue

import java.time.{LocalDate, LocalDateTime}

object TestData {

  val YEAR_2024 = 2024
  val MONTH_7 = 7
  val DAY_26 = 26
  val HOUR_12 = 12
  val MINUTES = 10
  val SECONDS = 55

  val LOCAL_DATE_TIME: LocalDateTime = LocalDateTime.of(YEAR_2024, MONTH_7, DAY_26, HOUR_12, MINUTES, SECONDS)
  val LOCAL_DATE_CURRENT: LocalDate = LocalDate.now

  val ENCRYPTED_VALUE = "sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI="
  val NONCE_VALUE: String = "RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3" +
    "fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"

  val ENCRYPTED_VALUE_OBJECT: EncryptedValue = EncryptedValue(ENCRYPTED_VALUE, NONCE_VALUE)
}
