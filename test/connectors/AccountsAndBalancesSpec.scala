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

package connectors

import utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, Json}
import utils.TestData.{
  accountNumber, dateString, eori, originatingSystem, testAckRef, testRegime, testStatus, testStatusText
}

class AccountsAndBalancesSpec extends SpecBase {

  "AccountsAndBalancesResponseContainer.returnParametersReads" should {
    import AccountsAndBalancesResponseContainer.returnParametersReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(returnParamJsString)) mustBe JsSuccess(returnParamOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"status\": \"pending\", \"eventId1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[ReturnParameters]
      }
    }
  }

  "AccountsAndBalancesResponseContainer.accountReads" should {
    import AccountsAndBalancesResponseContainer.accountReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(accountObJsString)) mustBe JsSuccess(accountOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"number1\": \"123456\", \"owner1\": \"test_owner\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[Account]
      }
    }
  }

  "AccountsAndBalancesResponseContainer.limitsReads" should {
    import AccountsAndBalancesResponseContainer.limitsReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(limitsObJsString)) mustBe JsSuccess(limitsOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"gaLimit\": \"pending\", \"periodLimit\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[Limits]
      }
    }
  }

  "AccountsAndBalancesResponseContainer.balancesReads" should {
    import AccountsAndBalancesResponseContainer.balancesReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(defermentBalanceObJsString)) mustBe JsSuccess(defermentBalanceOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"gaAvailBal\": \"1234\", \"accBal\": \"100\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[DefermentBalances]
      }
    }
  }

  "AccountsAndBalancesResponseContainer.generalGuaranteeAccountReads" should {
    import AccountsAndBalancesResponseContainer.generalGuaranteeAccountReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(generalGuaranteeAccountObJsString)) mustBe JsSuccess(generalGuaranteeAccountOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"acc\": \"pending\", \"gLimit\": \"100\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[GeneralGuaranteeAccount]
      }
    }
  }

  "AccountsAndBalancesResponseContainer.accountResponseDetailReads" should {
    import AccountsAndBalancesResponseContainer.accountResponseDetailReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(accountResponseDetailObJsString)) mustBe JsSuccess(accountResponseDetailOb)
    }
  }

  "AccountsAndBalancesResponseContainer.accountResponseCommonReads" should {
    import AccountsAndBalancesResponseContainer.accountResponseCommonReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(accountResponseCommonObJsString)) mustBe JsSuccess(accountResponseCommonOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"status1\": \"pending\", \"text\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[AccountResponseCommon]
      }
    }
  }

  "AccountsAndBalancesResponseContainer.accountsAndBalancesResponseReads" should {
    import AccountsAndBalancesResponseContainer.accountsAndBalancesResponseReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(accountsAndBalancesResponseObJsString)) mustBe JsSuccess(accountsAndBalancesResponseOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"resCom\": \"pending\", \"resDetail\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[AccountsAndBalancesResponse]
      }
    }
  }

  "AccountsAndBalancesResponseContainer.accountsAndBalancesResponseContainerReads" should {
    import AccountsAndBalancesResponseContainer.accountsAndBalancesResponseContainerReads

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(accaccountsAndBalancesResponseContainerObountObJsString)) mustBe JsSuccess(
        accountsAndBalancesResponseContainerOb
      )
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"accAndBal\": \"pending\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[AccountsAndBalancesResponseContainer]
      }
    }
  }

  "AccountsAndBalancesRequestContainer.accountsRequestCommonFormat" should {
    import AccountsAndBalancesRequestContainer.accountsRequestCommonFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(accountsRequestCommonObJsString)) mustBe JsSuccess(accountsRequestCommonOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"recDate\": \"2023-08-09\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[AccountsRequestCommon]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(accountsRequestCommonOb) mustBe Json.parse(accountsRequestCommonObJsString)
    }
  }

  "AccountsAndBalancesRequestContainer.accountsRequestDetailFormat" should {
    import AccountsAndBalancesRequestContainer.accountsRequestDetailFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(accountsRequestDetailObJsString)) mustBe JsSuccess(accountsRequestDetailOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"eoriNo1\": \"pending\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[AccountsRequestDetail]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(accountsRequestDetailOb) mustBe Json.parse(accountsRequestDetailObJsString)
    }
  }

  "AccountsAndBalancesRequestContainer.accountsAndBalancesRequestFormat" should {
    import AccountsAndBalancesRequestContainer.accountsAndBalancesRequestFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(accountsAndBalancesRequestObJsString)) mustBe JsSuccess(accountsAndBalancesRequestOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"reqCom\": \"pending\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[AccountsAndBalancesRequest]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(accountsAndBalancesRequestOb) mustBe Json.parse(accountsAndBalancesRequestObJsString)
    }
  }

  "AccountsAndBalancesRequestContainer.accountsAndBalancesRequestContainerFormat" should {
    import AccountsAndBalancesRequestContainer.accountsAndBalancesRequestContainerFormat

    "generate correct output for Json Reads" in new Setup {
      Json.fromJson(Json.parse(accountsAndBalancesRequestContainerObJsString)) mustBe JsSuccess(
        accountsAndBalancesRequestContainerOb
      )
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"accAndBal\": \"pending\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[AccountsAndBalancesRequestContainer]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(accountsAndBalancesRequestContainerOb) mustBe Json.parse(
        accountsAndBalancesRequestContainerObJsString
      )
    }
  }

  trait Setup {
    val returnParamOb: ReturnParameters = ReturnParameters("test_param", "test_param_value")
    val accountOb: Account              = Account("987654", "123456789", "12345678")
    val limitsOb: Limits                = Limits("100", "100")

    val defermentBalanceOb: DefermentBalances = DefermentBalances("100", "100")

    val generalGuaranteeAccountOb: GeneralGuaranteeAccount =
      GeneralGuaranteeAccount(accountOb, Some("999"), Some("900"))

    val accountResponseDetailOb: AccountResponseDetail =
      AccountResponseDetail(Some("123456789"), None, Some(Seq(generalGuaranteeAccountOb)))

    val accountResponseCommonOb: AccountResponseCommon =
      AccountResponseCommon(testStatus, Some(testStatusText), dateString, Some(Seq(returnParamOb)))

    val accountsAndBalancesResponseOb: AccountsAndBalancesResponse =
      AccountsAndBalancesResponse(Some(accountResponseCommonOb), accountResponseDetailOb)

    val accountsAndBalancesResponseContainerOb: AccountsAndBalancesResponseContainer =
      AccountsAndBalancesResponseContainer(accountsAndBalancesResponseOb)

    val accountsRequestCommonOb: AccountsRequestCommon = AccountsRequestCommon(
      PID = Some("test_id"),
      originatingSystem = Some(originatingSystem),
      receiptDate = dateString,
      acknowledgementReference = testAckRef,
      regime = testRegime
    )

    val accountsRequestDetailOb: AccountsRequestDetail = AccountsRequestDetail(
      EORINo = eori,
      accountType = Some("CDS Cash"),
      accountNumber = Some(accountNumber),
      referenceDate = None
    )

    val accountsAndBalancesRequestOb: AccountsAndBalancesRequest =
      AccountsAndBalancesRequest(accountsRequestCommonOb, accountsRequestDetailOb)

    val accountsAndBalancesRequestContainerOb: AccountsAndBalancesRequestContainer =
      AccountsAndBalancesRequestContainer(accountsAndBalancesRequestOb)

    val returnParamJsString: String = """{"paramName":"test_param","paramValue":"test_param_value"}""".stripMargin

    val accountObJsString: String =
      """{"number":"987654","type":"123456789","owner":"12345678","viewBalanceIsGranted":false}""".stripMargin

    val limitsObJsString: String = """{"periodGuaranteeLimit":"100","periodAccountLimit":"100"}""".stripMargin

    val defermentBalanceObJsString: String =
      """{"periodAvailableGuaranteeBalance":"100","periodAvailableAccountBalance":"100"}""".stripMargin

    val generalGuaranteeAccountObJsString: String =
      """{
        |"account":{
        |"number":"987654",
        |"type":"123456789",
        |"owner":"12345678",
        |"viewBalanceIsGranted":false
        |},
        |"guaranteeLimit":"999",
        |"availableGuaranteeBalance":"900"
        |}""".stripMargin

    val accountResponseDetailObJsString: String =
      """{
        |"EORINo":"123456789",
        |"generalGuaranteeAccount":[
        |{"account":{
        |"number":"987654",
        |"type":"123456789",
        |"owner":"12345678",
        |"viewBalanceIsGranted":false},
        |"guaranteeLimit":"999",
        |"availableGuaranteeBalance":"900"}]
        |}""".stripMargin

    val accountResponseCommonObJsString: String =
      """{
        |"status":"test_status",
        |"processingDate":"2020-07-28",
        |"statusText":"test_status_text",
        |"returnParameters":[{"paramName":"test_param","paramValue":"test_param_value"}]
        |}""".stripMargin

    val accountsAndBalancesResponseObJsString: String =
      """{"responseDetail":{"EORINo":"123456789",
        |"generalGuaranteeAccount":
        |[{"account":{
        |"number":"987654",
        |"type":"123456789",
        |"owner":"12345678",
        |"viewBalanceIsGranted":false
        |},
        |"guaranteeLimit":"999",
        |"availableGuaranteeBalance":"900"}]
        |},
        |"responseCommon":
        |{"status":"test_status",
        |"processingDate":"2020-07-28",
        |"statusText":"test_status_text",
        |"returnParameters":[{"paramName":"test_param","paramValue":"test_param_value"}]}}""".stripMargin

    val accaccountsAndBalancesResponseContainerObountObJsString: String =
      """{"accountsAndBalancesResponse":
        |{"responseDetail":{"EORINo":"123456789",
        |"generalGuaranteeAccount":[{"account":{"number":"987654",
        |"type":"123456789",
        |"owner":"12345678",
        |"viewBalanceIsGranted":false
        |},
        |"guaranteeLimit":"999",
        |"availableGuaranteeBalance":"900"}]
        |},
        |"responseCommon":
        |{"status":"test_status",
        |"processingDate":"2020-07-28",
        |"statusText":"test_status_text",
        |"returnParameters":[{"paramName":"test_param","paramValue":"test_param_value"}]}}
        |}""".stripMargin

    val accountsRequestCommonObJsString: String =
      """{
        |"receiptDate":"2020-07-28",
        |"regime":"CDS",
        |"PID":"test_id",
        |"originatingSystem":"ETMP",
        |"acknowledgementReference":"123dfeshsfgt34"
        |}""".stripMargin

    val accountsRequestDetailObJsString: String =
      """{"EORINo":"GB001","accountType":"CDS Cash","accountNumber":"987654"}""".stripMargin

    val accountsAndBalancesRequestObJsString: String =
      """{"requestCommon":{
        |"receiptDate":"2020-07-28",
        |"regime":"CDS",
        |"PID":"test_id",
        |"originatingSystem":"ETMP",
        |"acknowledgementReference":"123dfeshsfgt34"
        |},
        |"requestDetail":{"EORINo":"GB001","accountType":"CDS Cash","accountNumber":"987654"}}""".stripMargin

    val accountsAndBalancesRequestContainerObJsString: String =
      """{"accountsAndBalancesRequest":
        |{"requestCommon":
        |{"receiptDate":"2020-07-28",
        |"regime":"CDS",
        |"PID":"test_id",
        |"originatingSystem":"ETMP",
        |"acknowledgementReference":"123dfeshsfgt34"
        |},
        |"requestDetail":{"EORINo":"GB001","accountType":"CDS Cash","accountNumber":"987654"}}}""".stripMargin
  }
}
