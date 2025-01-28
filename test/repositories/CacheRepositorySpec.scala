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

package repositories

import models.EncryptedGuaranteeTransaction
import play.api.libs.json.{JsSuccess, Json}
import utils.SpecBase
import utils.TestData.{encryptedValue, encryptedValueObject, localDate, localDateTime, nonceValue}

import java.time.{Instant, ZoneOffset}

class CacheRepositorySpec extends SpecBase {

  "GuaranteeAccountMongo.format" should {

    "Read the incoming JsValue correctly" when {
      import GuaranteeAccountMongo.format

      "JsValue is correct Instant representation" in new Setup {
        Json.fromJson(Json.parse(guaranteeAccJsonString)) mustBe JsSuccess(guaranteeAcc)
      }

      "JsValue is not correct Instant representation" in new Setup {
        val actualGuaranteeAccOb: GuaranteeAccountMongo =
          Json.fromJson(Json.parse(guaranteeAccJsonStringWithIncorrectDateFormat)).get

        actualGuaranteeAccOb.transactions mustBe encryptedTransactions
        actualGuaranteeAccOb.lastUpdated.isInstanceOf[Instant] mustBe true
      }
    }

    "Write the object correctly" in new Setup {
      Json.toJson(guaranteeAcc) mustBe Json.parse(guaranteeAccJsonString)
    }
  }

  trait Setup {
    val encryptedTrans: EncryptedGuaranteeTransaction = EncryptedGuaranteeTransaction(
      date = localDate,
      movementReferenceNumber = encryptedValueObject,
      secureMovementReferenceNumber = None,
      balance = encryptedValueObject,
      uniqueConsignmentReference = None,
      declarantEori = encryptedValueObject,
      consigneeEori = encryptedValueObject,
      originalCharge = encryptedValueObject,
      dischargedAmount = encryptedValueObject,
      interestCharge = None,
      c18Reference = None,
      dueDates = Seq()
    )

    val encryptedTransactions: Seq[EncryptedGuaranteeTransaction] = Seq(encryptedTrans)
    val lastUpdatedTime: Instant                                  = localDateTime.toInstant(ZoneOffset.UTC)

    val guaranteeAcc: GuaranteeAccountMongo = GuaranteeAccountMongo(encryptedTransactions, lastUpdatedTime)

    val lastUpdatedDateString = """"$date":{"$numberLong":"1721995855000"}"""

    val guaranteeAccJsonString: String =
      s"""{
         |"transactions":[
         |{"date":"2024-07-29",
         |"movementReferenceNumber":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"balance":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"declarantEori":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"consigneeEori":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"originalCharge":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"dischargedAmount":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"dueDates":[]}],
         |"lastUpdated":{$lastUpdatedDateString}}""".stripMargin

    val guaranteeAccJsonStringWithIncorrectDateFormat: String =
      s"""{
         |"transactions":[
         |{"date":"2024-07-29",
         |"movementReferenceNumber":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"balance":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"declarantEori":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"consigneeEori":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"originalCharge":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"dischargedAmount":{"value":"$encryptedValue",
         |"nonce":"$nonceValue"},
         |"dueDates":[]}],
         |"lastUpdated":{"date":"2024-07-29T16:16:03.120694"}}""".stripMargin
  }
}
