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
import utils.TestData.{fromDate, toDate}
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class GuaranteeTransactionDatesSpec extends SpecBase {

  "format" should {
    "generate correct output for Json Reads" in new Setup {
      import GuaranteeTransactionDates.format

      Json.fromJson(Json.parse(ggTransDatesJsString)) mustBe JsSuccess(ggTransDatesOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(ggTransDatesOb) mustBe Json.parse(ggTransDatesJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"fromD\": \"pending\", \"toDate1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[GuaranteeTransactionDates]
      }
    }
  }

  trait Setup {
    val ggTransDatesOb: GuaranteeTransactionDates = GuaranteeTransactionDates(fromDate, toDate)

    val ggTransDatesJsString: String = """{"start":"2020-10-20","end":"2020-12-22"}""".stripMargin
  }
}
