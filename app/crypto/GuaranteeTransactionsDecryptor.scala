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

package crypto

import models.*
import uk.gov.hmrc.crypto.Crypted

import javax.inject.Inject

class GuaranteeTransactionsDecryptor @Inject() (crypto: CryptoAdapter) {
  def decryptGuaranteeTransactions(
    guaranteeTransactions: Seq[EncryptedGuaranteeTransaction]
  ): Seq[GuaranteeTransaction] = {
    def d(field: Either[EncryptedValue, Crypted]): String = crypto.decrypt(field)

    guaranteeTransactions.map { transaction =>
      GuaranteeTransaction(
        date = transaction.date,
        movementReferenceNumber = d(transaction.movementReferenceNumber),
        secureMovementReferenceNumber = transaction.secureMovementReferenceNumber,
        balance = BigDecimal(d(transaction.balance)),
        uniqueConsignmentReference = transaction.uniqueConsignmentReference.map(d),
        declarantEori = d(transaction.declarantEori),
        consigneeEori = d(transaction.consigneeEori),
        originalCharge = BigDecimal(d(transaction.originalCharge)),
        dischargedAmount = BigDecimal(d(transaction.dischargedAmount)),
        interestCharge = transaction.interestCharge.map(d),
        c18Reference = transaction.c18Reference.map(d),
        dueDates = decryptDueDates(transaction.dueDates)
      )
    }
  }

  private def decryptDueDates(dueDates: Seq[EncryptedDueDate]): Seq[DueDate] = {
    def d(field: Either[EncryptedValue, Crypted]): String = crypto.decrypt(field)

    dueDates.map { dueDate =>
      DueDate(
        dueDate = d(dueDate.dueDate),
        reasonForSecurity = dueDate.reasonForSecurity.map(d),
        amounts = decryptAmounts(dueDate.amounts),
        taxTypeGroups = dueDate.taxTypeGroups.map(group => decryptTaxTypeGroups(group))
      )
    }
  }

  private def decryptAmounts(amounts: EncryptedAmounts): Amounts = {
    def d(field: Either[EncryptedValue, Crypted]): String = crypto.decrypt(field)

    Amounts(
      totalAmount = d(amounts.totalAmount),
      clearedAmount = amounts.clearedAmount.map(d),
      openAmount = amounts.openAmount.map(d),
      updateDate = d(amounts.updateDate)
    )
  }

  private def decryptTaxTypeGroups(taxTypeGroup: EncryptedTaxTypeGroup): TaxTypeGroup = {
    def d(field: Either[EncryptedValue, Crypted]): String = crypto.decrypt(field)

    TaxTypeGroup(
      taxTypeGroup = d(taxTypeGroup.taxTypeGroup),
      amounts = decryptAmounts(taxTypeGroup.amounts),
      taxType = decryptTaxType(taxTypeGroup.taxType)
    )
  }

  private def decryptTaxType(taxType: EncryptedTaxType): TaxType = {
    def d(field: Either[EncryptedValue, Crypted]): String = crypto.decrypt(field)

    TaxType(
      taxType = d(taxType.taxType),
      amounts = decryptAmounts(taxType.amounts)
    )
  }
}
