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

package models

import play.api.libs.json._
import utils.SpecBase
import java.time.{Instant, LocalDateTime, ZoneOffset}

class MongoDateTimeFormatsSpec extends SpecBase {

  "MongoDateTimeFormats" should {

    "read" in new Setup {
      val testData: JsValue = Json.parse(""" {"$date": 1704228313308}  """)
      val res = MongoDateTimeFormats.localDateTimeRead.reads(testData)
      res.isSuccess mustBe true
    }

    "write DateTime" in new Setup {
      val res = MongoDateTimeFormats.localDateTimeWrite.writes(date)
      res mustBe testWrites
    }
  }

  trait Setup {
    val date = LocalDateTime.now()
    val testWrites: JsValue = Json.obj("$date" -> date.atZone(ZoneOffset.UTC).toInstant.toEpochMilli)
  }
}
