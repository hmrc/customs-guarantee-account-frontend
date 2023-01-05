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
