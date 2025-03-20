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
import uk.gov.hmrc.crypto.Crypted
import utils.SpecBase
import utils.TestData.{encryptedValue, localDate, localDateTime}

import java.time.{Instant, ZoneOffset}

class CacheRepositorySpec extends SpecBase {

  "GuaranteeAccountMongo.format" should {

    "Read the incoming JsValue correctly" when {
      import GuaranteeAccountMongo.format

      "JsValue is correct Instant representation when reading crypted value" in new Setup {
        Json.fromJson(Json.parse(newGuaranteeAccJsonString)) mustBe JsSuccess(newGuaranteeAcc)
      }

      "JsValue is not correct Instant representation" in new Setup {
        val actualGuaranteeAccOb: GuaranteeAccountMongo =
          Json.fromJson(Json.parse(guaranteeAccJsonStringWithIncorrectDateFormat)).get

        actualGuaranteeAccOb.transactions mustBe newEncryptedTransactions
        actualGuaranteeAccOb.lastUpdated.isInstanceOf[Instant] mustBe true
      }
    }

    "Write the object correctly" in new Setup {
      Json.toJson(newGuaranteeAcc) mustBe Json.parse(newGuaranteeAccJsonString)
    }
  }

  trait Setup {
    val encryptedValueObject: Crypted = Crypted(encryptedValue)

    val newEncryptedTrans: EncryptedGuaranteeTransaction = EncryptedGuaranteeTransaction(
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

    val newEncryptedTransactions: Seq[EncryptedGuaranteeTransaction] = Seq(newEncryptedTrans)
    val lastUpdatedTime: Instant = localDateTime.toInstant(ZoneOffset.UTC)
    val newGuaranteeAcc: GuaranteeAccountMongo = GuaranteeAccountMongo(newEncryptedTransactions, lastUpdatedTime)

    val lastUpdatedDateString = """"$date":{"$numberLong":"1721995855000"}"""

    val newGuaranteeAccJsonString: String =
      s"""{
         |"transactions":[
         |{"date":"2024-07-29",
         |"movementReferenceNumber":{"value":"$encryptedValue"},
         |"balance":{"value":"$encryptedValue"},
         |"declarantEori":{"value":"$encryptedValue"},
         |"consigneeEori":{"value":"$encryptedValue"},
         |"originalCharge":{"value":"$encryptedValue"},
         |"dischargedAmount":{"value":"$encryptedValue"},
         |"dueDates":[]}],
         |"lastUpdated":{$lastUpdatedDateString}}""".stripMargin

    val guaranteeAccJsonStringWithIncorrectDateFormat: String =
      s"""{
         |"transactions":[
         |{"date":"2024-07-29",
         |"movementReferenceNumber":{"value":"$encryptedValue"},
         |"balance":{"value":"$encryptedValue"},
         |"declarantEori":{"value":"$encryptedValue"},
         |"consigneeEori":{"value":"$encryptedValue"},
         |"originalCharge":{"value":"$encryptedValue"},
         |"dischargedAmount":{"value":"$encryptedValue"},
         |"dueDates":[]}],
         |"lastUpdated":{"date":"2024-07-29T16:16:03.120694"}}""".stripMargin
  }
}
