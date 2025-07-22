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

import uk.gov.hmrc.crypto.Crypted
import utils.SpecBase
import utils.TestData.{dateString, encryptedValue, localDate}
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class EncryptedGuaranteeTransactionSpec extends SpecBase {

  "amountFormat" should {
    import EncryptedGuaranteeTransaction.amountFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(encryptedAmountsObJsString)) mustBe JsSuccess(encryptedAmountsOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(encryptedAmountsOb) mustBe Json.parse(encryptedAmountsObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"totAmount\": \"pending\", \"open\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[EncryptedAmounts]
      }
    }
  }

  "taxTypeFormat" should {
    import EncryptedGuaranteeTransaction.taxTypeFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(encryptedTaxTypeObJsString)) mustBe JsSuccess(encryptedTaxTypeOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(encryptedTaxTypeOb) mustBe Json.parse(encryptedTaxTypeObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"totAmount\": \"pending\", \"open\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[EncryptedTaxType]
      }
    }
  }

  "taxTypeGroupFormat" should {
    import EncryptedGuaranteeTransaction.taxTypeGroupFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(encryptedTaxTypeGroupObJsString)) mustBe JsSuccess(encryptedTaxTypeGroupOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(encryptedTaxTypeGroupOb) mustBe Json.parse(encryptedTaxTypeGroupObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"totAmount\": \"pending\", \"open\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[EncryptedTaxTypeGroup]
      }
    }
  }

  "dueDateFormat" should {
    import EncryptedGuaranteeTransaction.dueDateFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(encryptedDueDateObJsString)) mustBe JsSuccess(encryptedDueDateOb)
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(encryptedDueDateOb) mustBe Json.parse(encryptedDueDateObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"totAmount\": \"pending\", \"open\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[EncryptedDueDate]
      }
    }
  }

  "format" should {
    import EncryptedGuaranteeTransaction.format

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(encryptedGuaranteeTransactionObJsString)) mustBe JsSuccess(
        encryptedGuaranteeTransactionOb
      )
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(encryptedGuaranteeTransactionOb) mustBe Json.parse(encryptedGuaranteeTransactionObJsString)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"totAmount\": \"pending\", \"open\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[EncryptedGuaranteeTransaction]
      }
    }
  }

  trait Setup {
    val cryptedAmount: Crypted = Crypted("100")

    val encryptedAmountsOb: EncryptedAmounts = EncryptedAmounts(
      totalAmount = cryptedAmount,
      clearedAmount = Some(cryptedAmount),
      openAmount = Some(cryptedAmount),
      updateDate = cryptedAmount
    )

    val encryptedTaxTypeOb: EncryptedTaxType = EncryptedTaxType(Crypted("VAT"), encryptedAmountsOb)

    val encryptedTaxTypeGroupOb: EncryptedTaxTypeGroup =
      EncryptedTaxTypeGroup(Crypted("TAX_GROUP"), encryptedAmountsOb, encryptedTaxTypeOb)

    val encryptedDueDateOb: EncryptedDueDate = EncryptedDueDate(
      dueDate = Crypted(dateString),
      reasonForSecurity = Some(Crypted("reason1")),
      amounts = encryptedAmountsOb,
      taxTypeGroups = Seq(encryptedTaxTypeGroupOb)
    )

    val encryptedValueObject: Crypted = Crypted(encryptedValue)

    val encryptedGuaranteeTransactionOb: EncryptedGuaranteeTransaction = EncryptedGuaranteeTransaction(
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

    val encryptedAmountsObJsString: String =
      """{
        |"totalAmount":{"value":"100"},
        |"updateDate":{"value":"100"},
        |"clearedAmount":{"value":"100"},
        |"openAmount":{"value":"100"}
        |}""".stripMargin

    val encryptedTaxTypeObJsString: String =
      """{
        |"taxType":{"value":"VAT"},
        |"amounts":{
        |"totalAmount":{"value":"100"},
        |"updateDate":{"value":"100"},
        |"clearedAmount":{"value":"100"},
        |"openAmount":{"value":"100"}
        |}
        |}""".stripMargin

    val encryptedTaxTypeGroupObJsString: String =
      """{"taxTypeGroup":
        |{"value":"TAX_GROUP"},
        |"amounts":{"totalAmount":{"value":"100"},
        |"updateDate":{"value":"100"},
        |"clearedAmount":{"value":"100"},
        |"openAmount":{"value":"100"}
        |},"taxType":{"taxType":{"value":"VAT"},
        |"amounts":{"totalAmount":{"value":"100"},
        |"updateDate":{"value":"100"},
        |"clearedAmount":{"value":"100"},
        |"openAmount":{"value":"100"}
        |}}}""".stripMargin

    val encryptedDueDateObJsString: String =
      """{
        |"dueDate":{"value":"2020-07-28"},
        |"amounts":{
        |"totalAmount":{"value":"100"},
        |"updateDate":{"value":"100"},
        |"clearedAmount":{"value":"100"},
        |"openAmount":{"value":"100"}
        |},
        |"taxTypeGroups":[{
        |"taxTypeGroup":{"value":"TAX_GROUP"},
        |"amounts":{
        |"totalAmount":{"value":"100"},
        |"updateDate":{"value":"100"},
        |"clearedAmount":{"value":"100"},
        |"openAmount":{"value":"100"}
        |},
        |"taxType":{
        |"taxType":{"value":"VAT"},
        |"amounts":{
        |"totalAmount":{"value":"100"},
        |"updateDate":{"value":"100"},
        |"clearedAmount":{"value":"100"},
        |"openAmount":{"value":"100"}
        |}}}],
        |"reasonForSecurity":{"value":"reason1"}}""".stripMargin

    val encryptedGuaranteeTransactionObJsString: String =
      """{
        |"consigneeEori":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI="},
        |"balance":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI="},
        |"dischargedAmount":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI="},
        |"date":"2024-07-29",
        |"originalCharge":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI="},
        |"declarantEori":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI="},
        |"movementReferenceNumber":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI="},
        |"dueDates":[]}""".stripMargin
  }
}
