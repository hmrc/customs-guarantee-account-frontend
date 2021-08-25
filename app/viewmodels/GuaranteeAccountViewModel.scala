/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package viewmodels

import helpers.Formatters
import models.{GuaranteeAccount, GuaranteeTransaction}
import play.api.i18n.Messages

import java.time.LocalDateTime

case class GuaranteeAccountViewModel(account: GuaranteeAccount,
                                     currentDateTime: LocalDateTime)(implicit messages: Messages) {
  val balanceAvailable: Option[String] = account.balances.map(bal => Formatters.formatCurrencyAmount0dp(bal.AvailableGuaranteeBalance))
  val limit: Option[String] = account.balances.map(bal => Formatters.formatCurrencyAmount0dp(bal.GuaranteeLimit))
  val accountUsage: Option[String] = account.balances.map(bal => Formatters.formatCurrencyAmount0dp(bal.usedFunds))
  val updatedDateTime: String = Formatters.updatedDateTime(currentDateTime)

  def taxCode(transaction: GuaranteeTransaction): Seq[Option[String]] = transaction.dueDates.map(_.reasonForSecurity)

}
