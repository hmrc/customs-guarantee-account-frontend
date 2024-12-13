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
import utils.TestData.{ENCRYPTED_VALUE, ENCRYPTED_VALUE_OBJECT, LOCAL_DATE, LOCAL_DATE_TIME, NONCE_VALUE}

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
      date = LOCAL_DATE,
      movementReferenceNumber = ENCRYPTED_VALUE_OBJECT,
      secureMovementReferenceNumber = None,
      balance = ENCRYPTED_VALUE_OBJECT,
      uniqueConsignmentReference = None,
      declarantEori = ENCRYPTED_VALUE_OBJECT,
      consigneeEori = ENCRYPTED_VALUE_OBJECT,
      originalCharge = ENCRYPTED_VALUE_OBJECT,
      dischargedAmount = ENCRYPTED_VALUE_OBJECT,
      interestCharge = None,
      c18Reference = None,
      dueDates = Seq()
    )

    val encryptedTransactions: Seq[EncryptedGuaranteeTransaction] = Seq(encryptedTrans)
    val lastUpdatedTime: Instant                                  = LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC)

    val guaranteeAcc: GuaranteeAccountMongo = GuaranteeAccountMongo(encryptedTransactions, lastUpdatedTime)

    val lastUpdatedDateString = """"$date":{"$numberLong":"1721995855000"}"""

    val guaranteeAccJsonString: String =
      s"""{
         |"transactions":[
         |{"date":"2024-07-29",
         |"movementReferenceNumber":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"balance":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"declarantEori":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"consigneeEori":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"originalCharge":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"dischargedAmount":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"dueDates":[]}],
         |"lastUpdated":{$lastUpdatedDateString}}""".stripMargin

    val guaranteeAccJsonStringWithIncorrectDateFormat: String =
      s"""{
         |"transactions":[
         |{"date":"2024-07-29",
         |"movementReferenceNumber":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"balance":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"declarantEori":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"consigneeEori":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"originalCharge":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"dischargedAmount":{"value":"$ENCRYPTED_VALUE",
         |"nonce":"$NONCE_VALUE"},
         |"dueDates":[]}],
         |"lastUpdated":{"date":"2024-07-29T16:16:03.120694"}}""".stripMargin
  }
}
