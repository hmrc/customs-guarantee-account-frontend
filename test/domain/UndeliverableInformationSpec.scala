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

package domain

import play.api.libs.json.{JsResultException, JsSuccess, Json}
import utils.SpecBase
import org.joda.time.DateTime
import utils.TestData.{day_26, hour_12, minutes_30, month_12, year_2024}

class UndeliverableInformationSpec extends SpecBase {

  "Writes" should {
    "generate the correct JsValue" in new Setup {
      Json.toJson(undelInfoOb) mustBe Json.parse(sampleResponse)
    }
  }

  "Reads" should {
    "generate the correct object" in new Setup {

      import UndeliverableInformation.format

      Json.fromJson(Json.parse(sampleResponse)) mustBe JsSuccess(undelInfoOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"id\": \"test_id\", \"eventId1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[UndeliverableInformationEvent]
      }
    }
  }

  trait Setup {
    val sampleResponse: String =
      """{
        |    "subject": "subject-example",
        |    "eventId": "example-id",
        |    "groupId": "example-group-id",
        |    "timestamp": "2024-12-26T12:30:00.000Z",
        |    "event": {
        |      "id": "example-id",
        |      "event": "someEvent",
        |      "emailAddress": "email@email.com",
        |      "detected": "2021-05-14T10:59:45.811+01:00",
        |      "code": 12,
        |      "reason": "Inbox full",
        |      "enrolment": "HMRC-CUS-ORG~EORINumber~GB744638982004"
        |    }
        |  }""".stripMargin

    val eventCode                                       = 12
    val undelInfoEventOb: UndeliverableInformationEvent = UndeliverableInformationEvent(
      "example-id",
      "someEvent",
      "email@email.com",
      "2021-05-14T10:59:45.811+01:00",
      Some(eventCode),
      Some("Inbox full"),
      "HMRC-CUS-ORG~EORINumber~GB744638982004"
    )

    val undelInfoOb: UndeliverableInformation = UndeliverableInformation(
      "subject-example",
      "example-id",
      "example-group-id",
      DateTime(year_2024, month_12, day_26, hour_12, minutes_30),
      undelInfoEventOb
    )
  }
}
