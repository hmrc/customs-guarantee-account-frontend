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

import connectors.{AccountStatusClosed, AccountStatusOpen, AccountStatusSuspended}
import models.{GeneralGuaranteeBalance, GuaranteeAccount}
import utils.SpecBase
import utils.TestData.balance

class GeneralGuaranteeAccountViewModelSpec extends SpecBase {

  "ViewModel" when {
    "account status is open" should {
      val account =
        GuaranteeAccount("GAN1234", "EORI1234", AccountStatusOpen, Some(GeneralGuaranteeBalance(balance, balance)))

      val viewModel = GeneralGuaranteeAccountViewModel(account)

      "have 'account-status-open' status html class attribute" in {
        viewModel.statusHtmlClassAttribute mustBe "account-status-open"
      }

      "have 'account-balance-status-open' balance html class attribute" in {
        viewModel.balanceHtmlClassAttribute mustBe "account-balance-status-open"
      }
    }

    "account status with no balances is open" should {
      val account   = GuaranteeAccount("GAN1234", "EORI1234", AccountStatusOpen, None)
      val viewModel = GeneralGuaranteeAccountViewModel(account)

      "have 'account-status-open' status html class attribute" in {
        viewModel.statusHtmlClassAttribute mustBe "account-status-open"
      }

      "have 'account-balance-status-open' balance html class attribute" in {
        viewModel.balanceHtmlClassAttribute mustBe "account-balance-status-open"
      }
    }

    "account status is suspended" should {
      val account =
        GuaranteeAccount("GAN1234", "EORI1234", AccountStatusSuspended, Some(GeneralGuaranteeBalance(balance, balance)))

      val viewModel = GeneralGuaranteeAccountViewModel(account)

      "have 'account-status-suspended' status html class attribute" in {
        viewModel.statusHtmlClassAttribute mustBe "account-status-suspended"
      }

      "have 'account-balance-status-suspended' balance html class attribute" in {
        viewModel.balanceHtmlClassAttribute mustBe "account-balance-status-suspended"
      }
    }

    "account status is closed" should {
      val account   =
        GuaranteeAccount("GAN1234", "EORI1234", AccountStatusClosed, Some(GeneralGuaranteeBalance(balance, balance)))
      val viewModel = GeneralGuaranteeAccountViewModel(account)

      "have 'account-status-closed' status html class attribute" in {
        viewModel.statusHtmlClassAttribute mustBe "account-status-closed"
      }

      "have 'account-balance-status-closed' balance html class attribute" in {
        viewModel.balanceHtmlClassAttribute mustBe "account-balance-status-closed"
      }
    }
  }
}
