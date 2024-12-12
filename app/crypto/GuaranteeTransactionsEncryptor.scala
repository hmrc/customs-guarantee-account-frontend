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

class GuaranteeTransactionsEncryptor @Inject() (crypto: AesGCMCrypto) {

  def encryptGuaranteeTransactions(
    guaranteeTransactions: Seq[GuaranteeTransaction],
    key: String
  ): Seq[EncryptedGuaranteeTransaction] = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, key)

    guaranteeTransactions.map { transaction =>
      EncryptedGuaranteeTransaction(
        date = transaction.date,
        movementReferenceNumber = e(transaction.movementReferenceNumber),
        secureMovementReferenceNumber = transaction.secureMovementReferenceNumber,
        balance = e(transaction.balance.toString),
        uniqueConsignmentReference = transaction.uniqueConsignmentReference.map(e),
        declarantEori = e(transaction.declarantEori),
        consigneeEori = e(transaction.consigneeEori),
        originalCharge = e(transaction.originalCharge.toString),
        dischargedAmount = e(transaction.dischargedAmount.toString),
        interestCharge = transaction.interestCharge.map(e),
        c18Reference = transaction.c18Reference.map(e),
        dueDates = encryptDueDates(transaction.dueDates, key)
      )
    }
  }

  private def encryptDueDates(dueDates: Seq[DueDate], key: String): Seq[EncryptedDueDate] = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, key)

    dueDates.map { dueDate =>
      EncryptedDueDate(
        dueDate = e(dueDate.dueDate),
        reasonForSecurity = dueDate.reasonForSecurity.map(e),
        amounts = encryptAmounts(dueDate.amounts, key),
        taxTypeGroups = dueDate.taxTypeGroups.map(group => encryptTaxTypeGroups(group, key))
      )
    }
  }

  private def encryptAmounts(amounts: Amounts, key: String): EncryptedAmounts = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, key)

    EncryptedAmounts(
      totalAmount = e(amounts.totalAmount),
      clearedAmount = amounts.clearedAmount.map(e),
      openAmount = amounts.openAmount.map(e),
      updateDate = e(amounts.updateDate)
    )
  }

  private def encryptTaxTypeGroups(taxTypeGroup: TaxTypeGroup, key: String): EncryptedTaxTypeGroup = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, key)

    EncryptedTaxTypeGroup(
      taxTypeGroup = e(taxTypeGroup.taxTypeGroup),
      amounts = encryptAmounts(taxTypeGroup.amounts, key),
      taxType = encryptTaxType(taxTypeGroup.taxType, key)
    )
  }

  private def encryptTaxType(taxType: TaxType, key: String): EncryptedTaxType = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, key)

    EncryptedTaxType(
      taxType = e(taxType.taxType),
      amounts = encryptAmounts(taxType.amounts, key)
    )
  }
}
