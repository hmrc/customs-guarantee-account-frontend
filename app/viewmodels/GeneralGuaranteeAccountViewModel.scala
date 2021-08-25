/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package viewmodels

import connectors.CDSAccountStatus
import models.{GeneralGuaranteeBalance, GuaranteeAccount}
import views.helpers.HtmlHelper._

case class GeneralGuaranteeAccountViewModel(gan: String,
                                            status: CDSAccountStatus,
                                            statusHtmlClassAttribute: String,
                                            balanceHtmlClassAttribute: String,
                                            balances: Option[GeneralGuaranteeBalance])
object GeneralGuaranteeAccountViewModel {

  def apply(account: GuaranteeAccount):GeneralGuaranteeAccountViewModel = {
    new GeneralGuaranteeAccountViewModel(
      account.number,
      account.status,
      account.status.statusAttribute,
      account.status.balanceAttribute,
      account.balances
    )
  }
}
