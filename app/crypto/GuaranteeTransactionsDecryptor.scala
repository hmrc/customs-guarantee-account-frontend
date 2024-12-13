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

import models._

import javax.inject.Inject

class GuaranteeTransactionsDecryptor @Inject() (crypto: AesGCMCrypto) {
  def decryptGuaranteeTransactions(
    guaranteeTransactions: Seq[EncryptedGuaranteeTransaction],
    key: String
  ): Seq[GuaranteeTransaction] = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, key)

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
        dueDates = decryptDueDates(transaction.dueDates, key)
      )
    }
  }

  private def decryptDueDates(dueDates: Seq[EncryptedDueDate], key: String): Seq[DueDate] = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, key)

    dueDates.map { dueDate =>
      DueDate(
        dueDate = d(dueDate.dueDate),
        reasonForSecurity = dueDate.reasonForSecurity.map(d),
        amounts = decryptAmounts(dueDate.amounts, key),
        taxTypeGroups = dueDate.taxTypeGroups.map(group => decryptTaxTypeGroups(group, key))
      )
    }
  }

  private def decryptAmounts(amounts: EncryptedAmounts, key: String): Amounts = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, key)

    Amounts(
      totalAmount = d(amounts.totalAmount),
      clearedAmount = amounts.clearedAmount.map(d),
      openAmount = amounts.openAmount.map(d),
      updateDate = d(amounts.updateDate)
    )
  }

  private def decryptTaxTypeGroups(taxTypeGroup: EncryptedTaxTypeGroup, key: String): TaxTypeGroup = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, key)

    TaxTypeGroup(
      taxTypeGroup = d(taxTypeGroup.taxTypeGroup),
      amounts = decryptAmounts(taxTypeGroup.amounts, key),
      taxType = decryptTaxType(taxTypeGroup.taxType, key)
    )
  }

  private def decryptTaxType(taxType: EncryptedTaxType, key: String): TaxType = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, key)

    TaxType(
      taxType = d(taxType.taxType),
      amounts = decryptAmounts(taxType.amounts, key)
    )
  }
}
