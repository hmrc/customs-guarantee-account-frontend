/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package viewmodels

import connectors.{AccountStatusClosed, AccountStatusOpen, AccountStatusSuspended}
import models.{GeneralGuaranteeBalance, GuaranteeAccount}
import utils.SpecBase

class GeneralGuaranteeAccountViewModelSpec extends SpecBase {

  "ViewModel" when {

    "account status is open" should {
      val account = GuaranteeAccount("GAN1234", "EORI1234", AccountStatusOpen, Some(GeneralGuaranteeBalance(100, 100))) //scalastyle:off magic.number
      val viewModel = GeneralGuaranteeAccountViewModel(account)

      "have 'account-status-open' status html class attribute" in {
        viewModel.statusHtmlClassAttribute mustBe "account-status-open"
      }

      "have 'account-balance-status-open' balance html class attribute" in {
        viewModel.balanceHtmlClassAttribute mustBe "account-balance-status-open"
      }

    }

    "account status with no balances is open" should {
      val account = GuaranteeAccount("GAN1234", "EORI1234", AccountStatusOpen, None) //scalastyle:off magic.number
      val viewModel = GeneralGuaranteeAccountViewModel(account)

      "have 'account-status-open' status html class attribute" in {
        viewModel.statusHtmlClassAttribute mustBe "account-status-open"
      }

      "have 'account-balance-status-open' balance html class attribute" in {
        viewModel.balanceHtmlClassAttribute mustBe "account-balance-status-open"
      }

    }

    "account status is suspended" should {
      val account = GuaranteeAccount("GAN1234", "EORI1234", AccountStatusSuspended, Some(GeneralGuaranteeBalance(100, 100))) //scalastyle:off magic.number
      val viewModel = GeneralGuaranteeAccountViewModel(account)

      "have 'account-status-suspended' status html class attribute" in {
        viewModel.statusHtmlClassAttribute mustBe "account-status-suspended"
      }

      "have 'account-balance-status-suspended' balance html class attribute" in {
        viewModel.balanceHtmlClassAttribute mustBe "account-balance-status-suspended"
      }

    }

    "account status is closed" should {
      val account = GuaranteeAccount("GAN1234", "EORI1234", AccountStatusClosed, Some(GeneralGuaranteeBalance(100, 100))) //scalastyle:off magic.number
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
