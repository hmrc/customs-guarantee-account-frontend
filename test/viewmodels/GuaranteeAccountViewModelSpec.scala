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

package viewmodels

import config.AppConfig
import connectors.AccountStatusOpen
import models.{GeneralGuaranteeBalance, GuaranteeAccount}
import play.api.test.Helpers
import utils.SpecBase

import java.time.LocalDateTime

class GuaranteeAccountViewModelSpec extends SpecBase  {
  val guaranteeAccount = GuaranteeAccount("gan", "eori", AccountStatusOpen,
                                                 Some(GeneralGuaranteeBalance(BigDecimal(10000.50), BigDecimal(5000.10))))

  implicit val appConfig = mock[AppConfig]
  
  val model = GuaranteeAccountViewModel(guaranteeAccount, LocalDateTime.parse("2020-04-08T12:30"))(Helpers.stubMessages())

  "GuaranteeAccountViewModel" should {
    "include the formatted available balance" in {
      model.balanceAvailable mustBe Some("£5,000.10")
    }

    "include the formatted account limit" in {
      model.limit mustBe Some("£10,000.50")
    }

    "include the formatted account usage amount" in {
      model.accountUsage mustBe Some("£5,000.40")
    }

    "include updated date time" in {
      model.updatedDateTime mustBe "12:30 pm cf.guarantee-account.updated.time.on 8 month.4 2020"
    }
  }
}

