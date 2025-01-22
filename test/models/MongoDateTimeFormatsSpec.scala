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
import utils.TestData.{dateTime, dateInMilliSeconds, dollar}
import java.time.ZoneOffset

class MongoDateTimeFormatsSpec extends SpecBase {

  "MongoDateTimeFormats" should {

    "read datetime from json string" in new Setup {

      val testData: JsValue = Json.parse(" {\"" + dollar + "date\": " + dateInMilliSeconds + "}  ")
      val res               = MongoDateTimeFormats.localDateTimeRead.reads(testData)

      res.isSuccess mustBe true
      res.get.toInstant(ZoneOffset.UTC).toEpochMilli mustEqual dateInMilliSeconds
    }

    "write DateTime" in new Setup {
      val res = MongoDateTimeFormats.localDateTimeWrite.writes(dateTime)

      res mustBe testWrites
    }
  }

  trait Setup {
    val testWrites: JsValue = Json.obj("$" + "date" -> dateInMilliSeconds)
  }
}
