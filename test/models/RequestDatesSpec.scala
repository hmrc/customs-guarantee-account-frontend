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

import utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, Json}
import utils.TestData.{fromDate, toDate}

class RequestDatesSpec extends SpecBase {

  "format" should {
    "generate correct output for Json Reads" in new Setup {
      import RequestDates.requestDates

      Json.fromJson(Json.parse(requestDatesJsString)) mustBe JsSuccess(requestDatesOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(requestDatesOb) mustBe Json.parse(requestDatesJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"fromD\": \"pending\", \"toDate1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[RequestDates]
      }
    }
  }

  trait Setup {
    val requestDatesOb: RequestDates = RequestDates(fromDate, toDate)
    val requestDatesJsString: String = """{"dateFrom":"2020-10-20","dateTo":"2020-12-22"}""".stripMargin
  }
}
