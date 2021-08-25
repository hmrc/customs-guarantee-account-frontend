/*
 * Copyright 2021 HM Revenue & Customs
 *
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

