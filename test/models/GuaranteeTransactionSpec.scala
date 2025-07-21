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
import utils.TestData.localDate
import play.api.libs.json.{JsResultException, JsSuccess, Json}

class GuaranteeTransactionSpec extends SpecBase {

  "GuaranteeTransaction.fieldNames" should {

    "return the correct result" in new Setup {
      ggTransactionOb.fieldNames mustBe Seq(
        "date",
        "movementReferenceNumber",
        "secureMovementReferenceNumber",
        "balance",
        "uniqueConsignmentReference",
        "declarantEori",
        "consigneeEori",
        "originalCharge",
        "dischargedAmount",
        "interestCharge",
        "c18Reference",
        "dueDates"
      )
    }
  }

  "GuaranteeTransaction.moreThanOne" should {
    "return the correct result" in new Setup {
      ggTransactionOb.moreThanOne mustBe false
    }
  }

  "GuaranteeTransaction.amountReads" should {
    import GuaranteeTransaction.amountReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(amountsObJsString)) mustBe JsSuccess(amountsOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"fromD\": \"pending\", \"toDate1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[Amounts]
      }
    }
  }

  "GuaranteeTransaction.taxTypeReads" should {
    import GuaranteeTransaction.taxTypeReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxTypeObJsString)) mustBe JsSuccess(taxTypeOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"fromD\": \"pending\", \"toDate1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxType]
      }
    }
  }

  "GuaranteeTransaction.taxTypeGroupReads" should {
    import GuaranteeTransaction.taxTypeGroupReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(taxTypeGroupObJsString)) mustBe JsSuccess(taxTypeGroupOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"fromD\": \"pending\", \"toDate1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[TaxTypeGroup]
      }
    }
  }

  "GuaranteeTransaction.dueDateReads" should {
    import GuaranteeTransaction.dueDateReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(dueDateObJsString)) mustBe JsSuccess(dueDateOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"fromD\": \"pending\", \"toDate1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[DueDate]
      }
    }
  }

  "GuaranteeTransaction.guaranteeTransactionReads" should {
    import GuaranteeTransaction.guaranteeTransactionReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(ggTransactionObJsString)) mustBe JsSuccess(ggTransactionOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"date1\": \"2023-01-19\", \"ref\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[GuaranteeTransaction]
      }
    }
  }

  "getSecurityReason" should {
    "return None when dueDates are empty " in new Setup {
      ddNoTaxTypeGroups.securityReason mustBe empty
    }

    "return security reason  when dueDates are not empty " in new Setup {
      Some(dueDateOb.securityReason.get.taxCode) mustBe dueDateOb.reasonForSecurity
    }

    "return None when reasonForSecurity and taxTypeGroups are empty " in new Setup {
      ddNoTaxTypeGroups.copy(reasonForSecurity = None, taxTypeGroups = None).securityReason mustBe empty
    }
  }

  trait Setup {
    val amountsOb: Amounts = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
    val taxTypeOb: TaxType = TaxType("VAT", amountsOb)

    val taxTypeGroupOb: TaxTypeGroup = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amountsOb, taxType = taxTypeOb)

    val dueDateOb: DueDate = DueDate(
      dueDate = "2020-07-28",
      reasonForSecurity = Some("T24"),
      amounts = amountsOb,
      taxTypeGroups = Seq(taxTypeGroupOb)
    )

    val ddNoTaxTypeGroups: DueDate =
      DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amountsOb, taxTypeGroups = Seq.empty)

    val ggTransactionOb: GuaranteeTransaction = GuaranteeTransaction(
      localDate,
      "19GB000056HG5w746",
      None,
      BigDecimal(45367.12),
      Some("MGH-500000"),
      "GB10000",
      "GB20000",
      BigDecimal(21.00),
      BigDecimal(11.50),
      None,
      None,
      dueDates = Seq(dueDateOb)
    )

    val amountsObJsString: String =
      """{"totalAmount":"20.00","updateDate":"2020-08-01","clearedAmount":"30.00","openAmount":"10.00"}""".stripMargin

    val taxTypeObJsString: String =
      """{"taxType":"VAT",
        |"amounts":{
        |"totalAmount":"20.00","updateDate":"2020-08-01","clearedAmount":"30.00","openAmount":"10.00"
        |}
        |}""".stripMargin

    val taxTypeGroupObJsString: String =
      """{
        |"taxTypeGroup":"VAT",
        |"amounts":{"totalAmount":"20.00","updateDate":"2020-08-01","clearedAmount":"30.00","openAmount":"10.00"},
        |"taxType":{
        |"taxType":"VAT",
        |"amounts":{"totalAmount":"20.00","updateDate":"2020-08-01","clearedAmount":"30.00","openAmount":"10.00"}
        |}
        |}""".stripMargin

    val dueDateObJsString: String =
      """{
        |"dueDate":"2020-07-28",
        |"amounts":{"totalAmount":"20.00","updateDate":"2020-08-01","clearedAmount":"30.00","openAmount":"10.00"},
        |"taxTypeGroups":[
        |{
        |"taxTypeGroup":"VAT",
        |"amounts":{"totalAmount":"20.00","updateDate":"2020-08-01","clearedAmount":"30.00","openAmount":"10.00"},
        |"taxType":{
        |"taxType":"VAT",
        |"amounts":{"totalAmount":"20.00","updateDate":"2020-08-01","clearedAmount":"30.00","openAmount":"10.00"}}}],
        |"reasonForSecurity":"T24"}""".stripMargin

    val ggTransactionObJsString: String =
      """{
        |"consigneeEori":"GB20000",
        |"balance":45367.12,
        |"dischargedAmount":11.5,
        |"originalCharge":21,
        |"declarantEori":"GB10000",
        |"movementReferenceNumber":"19GB000056HG5w746",
        |"dueDates":[
        |{"dueDate":"2020-07-28",
        |"amounts":{"totalAmount":"20.00","updateDate":"2020-08-01","clearedAmount":"30.00","openAmount":"10.00"},
        |"taxTypeGroups":[
        |{"taxTypeGroup":"VAT",
        |"amounts":{"totalAmount":"20.00","updateDate":"2020-08-01","clearedAmount":"30.00","openAmount":"10.00"},
        |"taxType":{
        |"taxType":"VAT",
        |"amounts":{"totalAmount":"20.00","updateDate":"2020-08-01","clearedAmount":"30.00","openAmount":"10.00"}}}
        |],
        |"reasonForSecurity":"T24"}],"date":"2024-07-29","uniqueConsignmentReference":"MGH-500000"}""".stripMargin

  }
}
