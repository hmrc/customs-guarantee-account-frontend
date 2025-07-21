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
import utils.TestData.{eighteen, twoThousand}
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class GuaranteeAccountSpec extends SpecBase {

  "usedFunds" should {
    "return correct value" in {
      val expectedResult = 1982

      GeneralGuaranteeBalance(BigDecimal(twoThousand), BigDecimal(eighteen)).usedFunds mustBe BigDecimal(expectedResult)
    }
  }

  "usedPercentage" should {
    "return correct value" in {
      val expectedPercentage = 99.100

      GeneralGuaranteeBalance(BigDecimal(twoThousand), BigDecimal(eighteen)).usedPercentage mustBe BigDecimal(
        expectedPercentage
      )
    }
  }

  "GeneralGuaranteeBalance.format" should {
    "generate correct output for Json Reads" in new Setup {
      import GeneralGuaranteeBalance.format

      Json.fromJson(Json.parse(ggBalanceObJsString)) mustBe JsSuccess(ggBalanceOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(ggBalanceOb) mustBe Json.parse(ggBalanceObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"gaLimit\": \"100\", \"avlBalance\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[GeneralGuaranteeBalance]
      }
    }
  }

  trait Setup {
    val ggBalanceOb: GeneralGuaranteeBalance = GeneralGuaranteeBalance(BigDecimal(twoThousand), BigDecimal(twoThousand))
    val ggBalanceObJsString: String          = """{"GuaranteeLimit":2000,"AvailableGuaranteeBalance":2000}""".stripMargin
  }
}
