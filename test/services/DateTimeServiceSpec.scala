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

package services

import config.AppConfig
import utils.SpecBase

import java.time._

class DateTimeServiceSpec extends SpecBase {

  "DateTimeService" should {
    "return the future date as system date/time when fixed-systemdate-for-tests is enabled" in new Setup {
      val newApp = application.configure("features.fixed-systemdate-for-tests" ->  true)
      override val appConfig = newApp.injector().instanceOf[AppConfig]
      override val service = new DateTimeService(appConfig)
      val testTime = service.systemDateTime(ZoneId.systemDefault())
      testTime.isEqual(fixedTestDateTime) mustBe true
    }

    "return the future date as local date time when fixed-systemdate-for-tests is enabled" in  new Setup {
      val newApp = application.configure("features.fixed-systemdate-for-tests" ->  true)
      override val appConfig = newApp.injector().instanceOf[AppConfig]
      override val service = new DateTimeService(appConfig)
      val testTime = service.localDateTime()
      testTime.isEqual(fixedTestDateTime) mustBe true
    }

    "return the future date as utc when fixed-systemdate-for-tests is enabled" in  new Setup {
      val newApp = application.configure("features.fixed-systemdate-for-tests" ->  true)
      override val appConfig = newApp.injector().instanceOf[AppConfig]
      override val service = new DateTimeService(appConfig)
      val testTime = service.utcDateTime()
      testTime.isEqual(fixedTestDateTime) mustBe true
    }

    "return the time now when fixed-systemdate-for-tests is disabled" in new Setup {
      val newApp = application.configure("features.fixed-systemdate-for-tests" ->  false)
      override val appConfig = newApp.injector().instanceOf[AppConfig]
      override val service = new DateTimeService(appConfig)

      val testTime = service.systemDateTime(ZoneId.systemDefault())
      testTime.isBefore(fixedTestDateTime) mustBe true
    }
  }
  trait Setup {
    val fixedTestDateTime = LocalDateTime.of(LocalDate.of(2027, 12, 20), LocalTime.of(12,30))
    val appConfig = application.injector().instanceOf[AppConfig]
    val service = new DateTimeService(appConfig)
  }
}
