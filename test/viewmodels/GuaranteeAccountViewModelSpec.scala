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

import connectors.AccountStatusOpen
import models.{Amounts, DueDate, GeneralGuaranteeBalance, GuaranteeAccount, GuaranteeTransaction, TaxType, TaxTypeGroup}
import play.api.test.Helpers
import utils.SpecBase
import utils.TestData.{year_2019, dayOne}

import java.time.{LocalDate, LocalDateTime, Month}

class GuaranteeAccountViewModelSpec extends SpecBase {

  "GuaranteeAccountViewModel" should {

    "include the usedPercentage and usedFunds" in new Setup {
      model02.account.balances.get.usedFunds mustBe BigDecimal(-5000.1)
      model02.account.balances.get.usedPercentage mustBe BigDecimal(0)
    }

    "return security reasons from duedates" in new Setup {

      val amt = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
      val ttg = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = TaxType("VAT", amt))
      val dd  = DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg))

      val guranteeTxn = GuaranteeTransaction(
        LocalDate.of(year_2019, Month.OCTOBER, dayOne),
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
        dueDates = Seq(dd)
      )

      model02.taxCode(guranteeTxn).head.get mustEqual "T24"
    }

    "include the formatted available balance" in new Setup {
      model.balanceAvailable mustBe Some("£5,000.10")
    }

    "include the formatted account limit" in new Setup {
      model.limit mustBe Some("£10,000.50")
    }

    "include the formatted account usage amount" in new Setup {
      model.accountUsage mustBe Some("£5,000.40")
    }

    "include updated date time" in new Setup {
      model.updatedDateTime mustBe "12:30 pm cf.guarantee-account.updated.time.on 8 month.4 2020"
    }
  }

  trait Setup {

    val guaranteeAccount = GuaranteeAccount(
      "gan",
      "eori",
      AccountStatusOpen,
      Some(GeneralGuaranteeBalance(BigDecimal(10000.50), BigDecimal(5000.10)))
    )

    val guaranteeAccount02 = GuaranteeAccount(
      "gan",
      "eori",
      AccountStatusOpen,
      Some(GeneralGuaranteeBalance(BigDecimal(0), BigDecimal(5000.10)))
    )

    val model =
      GuaranteeAccountViewModel(guaranteeAccount, LocalDateTime.parse("2020-04-08T12:30"))(Helpers.stubMessages())

    val model02 =
      GuaranteeAccountViewModel(guaranteeAccount02, LocalDateTime.parse("2020-04-08T12:30"))(Helpers.stubMessages())
  }
}
