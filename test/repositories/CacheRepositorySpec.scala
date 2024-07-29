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
import utils.TestData.{ENCRYPTED_VALUE_OBJECT, LOCAL_DATE_CURRENT, LOCAL_DATE_TIME}

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
      date = LOCAL_DATE_CURRENT,
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
    val lastUpdatedTime: Instant = LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC)

    val guaranteeAcc: GuaranteeAccountMongo = GuaranteeAccountMongo(encryptedTransactions, lastUpdatedTime)

    val guaranteeAccWithDefaultDateTime: GuaranteeAccountMongo =
      GuaranteeAccountMongo(encryptedTransactions, Instant.now())

    val guaranteeAccJsonString: String =
      """{
        |"transactions":[
        |{"date":"2024-07-29",
        |"movementReferenceNumber":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"balance":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"declarantEori":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"consigneeEori":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"originalCharge":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"dischargedAmount":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"dueDates":[]}],
        |"lastUpdated":{"$date":{"$numberLong":"1721995855000"}}}""".stripMargin

    val guaranteeAccJsonStringWithIncorrectDateFormat: String =
      """{
        |"transactions":[
        |{"date":"2024-07-29",
        |"movementReferenceNumber":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"balance":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"declarantEori":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"consigneeEori":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"originalCharge":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"dischargedAmount":{"value":"sTe+0SVx5j5y509Nq8tIyflvnsRMfMC5Ae03fNUEarI=",
        |"nonce":"RosGoD7PB/RGTz9uYEvU86zB/LxuWRUGQ2ay9PYbqWBKgy1Jy+j+REmx+cp74VhtvTrfFttQv4ArHUc/1tMyl3fGz3/cr8Tm1BHzanv659kI2MJqMynltIsY9fqdDpmO"},
        |"dueDates":[]}],
        |"lastUpdated":{"date":"2024-07-22"}}""".stripMargin
  }
}
