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

import utils.SpecBase
import org.joda.time
import java.time._
import java.util.Date

class DateConvertersSpec extends SpecBase {

  "toLocalDate" should {
    "convert date to localdate" in new Setup {
      val testDate: Date = new Date
      val res = DateConverters.toLocalDate(testDate)
      val compare: LocalDate = testDate.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
      compare mustBe res
    }
  }

  "OrderedLocalDate Compare" should {
    "Comparing the same date returns 0" in new Setup {
      val compare = DateConverters.OrderedLocalDate(date).compare(date)
      compare mustBe 0
    }

    "Comparing different dates returns difference between" in new Setup {
      val result = -1999999998
      val minDate = LocalDate.MIN
      val maxDate = LocalDate.MAX
      val compare = DateConverters.OrderedLocalDate(minDate).compare(maxDate)
      compare mustBe result
    }
  }

  "toJodaTime" should {
    "successfully convert to a joda date time" in new Setup {
      val res = DateConverters.toJodaTime(date)
      val compare = new time.LocalDate(java.util.Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC)))
      compare mustBe res
    }
  }

  trait Setup {
    val date = LocalDate.now();
  }
}
