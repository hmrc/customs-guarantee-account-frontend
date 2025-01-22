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

import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import services.DateTimeService
import utils.SpecBase
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application

import java.time.{LocalDate, LocalDateTime}

class FormattersSpec extends SpecBase {
  val mockDateTimeService: DateTimeService = mock[DateTimeService]

  when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-19T09:30:59"))

  "filenameWithDate" should {
    "return a correctly formatted filename" in {
      running(application) {
        val result = Formatters.filenameWithDateTime()(messages, mockDateTimeService)

        result must be("Open_Guarantees_20200419093059.CSV")
      }
    }
  }

  "filenameWithRequestDates" should {
    "return a correctly formatted filename for the given dates" in {
      val fromDate = LocalDate.parse("2020-11-06")
      val toDate   = LocalDate.parse("2020-12-12")
      running(application) {
        val result = Formatters.filenameWithRequestDates(fromDate, toDate)(messages)

        result must be("Open_Guarantees_202011-202012.CSV")
      }
    }
  }
}
