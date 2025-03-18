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

class GuaranteeTransactionsEncryptor @Inject() (crypto: CryptoAdapter) {

  def encryptGuaranteeTransactions(
    guaranteeTransactions: Seq[GuaranteeTransaction]
  ): Seq[EncryptedGuaranteeTransaction] = {
    def e(field: String): Either[EncryptedValue, Crypted] = crypto.encrypt(field)

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
        dueDates = encryptDueDates(transaction.dueDates)
      )
    }
  }

  private def encryptDueDates(dueDates: Seq[DueDate]): Seq[EncryptedDueDate] = {
    def e(field: String): Either[EncryptedValue, Crypted] = crypto.encrypt(field)

    dueDates.map { dueDate =>
      EncryptedDueDate(
        dueDate = e(dueDate.dueDate),
        reasonForSecurity = dueDate.reasonForSecurity.map(e),
        amounts = encryptAmounts(dueDate.amounts),
        taxTypeGroups = dueDate.taxTypeGroups.map(group => encryptTaxTypeGroups(group))
      )
    }
  }

  private def encryptAmounts(amounts: Amounts): EncryptedAmounts = {
    def e(field: String): Either[EncryptedValue, Crypted] = crypto.encrypt(field)

    EncryptedAmounts(
      totalAmount = e(amounts.totalAmount),
      clearedAmount = amounts.clearedAmount.map(e),
      openAmount = amounts.openAmount.map(e),
      updateDate = e(amounts.updateDate)
    )
  }

  private def encryptTaxTypeGroups(taxTypeGroup: TaxTypeGroup): EncryptedTaxTypeGroup = {
    def e(field: String): Either[EncryptedValue, Crypted] = crypto.encrypt(field)

    EncryptedTaxTypeGroup(
      taxTypeGroup = e(taxTypeGroup.taxTypeGroup),
      amounts = encryptAmounts(taxTypeGroup.amounts),
      taxType = encryptTaxType(taxTypeGroup.taxType)
    )
  }

  private def encryptTaxType(taxType: TaxType): EncryptedTaxType = {
    def e(field: String): Either[EncryptedValue, Crypted] = crypto.encrypt(field)

    EncryptedTaxType(
      taxType = e(taxType.taxType),
      amounts = encryptAmounts(taxType.amounts)
    )
  }
}
