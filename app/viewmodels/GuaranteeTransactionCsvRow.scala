/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{DueDate, GuaranteeTransaction, TaxTypeGroup}
import play.api.i18n.Messages

import java.time.format.DateTimeFormatter

case class GuaranteeTransactionCsvRow(date: Option[String] = None,
                                      movementReferenceNumber: Option[String] = None,
                                      uniqueConsignmentReference: Option[String] = None,
                                      consigneeEori: Option[String] = None,
                                      declarantEori: Option[String] = None,
                                      totalOriginalCharge: Option[BigDecimal] = None,
                                      totalDischargedAmount: Option[BigDecimal] = None,
                                      totalBalance: Option[BigDecimal] = None,
                                      interestCharge: Option[BigDecimal] = None,
                                      c18Reference: Option[String] = None,
                                      securityReasonBreakdown: Option[String] = None,
                                      taxCode: Option[String] = None,
                                      expiryDate: Option[String] = None,
                                      originalCharge: Option[BigDecimal] = None,
                                      dischargedAmount: Option[BigDecimal] = None,
                                      balance: Option[BigDecimal] = None,
                                      lastUpdated: Option[String] = None)

object GuaranteeTransactionCsvRow {

  implicit class GuaranteeTransactionCsvRowViewModel(guaranteeTransaction: GuaranteeTransaction)
                                                    (implicit messages: Messages) {

    private val indentation = "   "

    def toReportLayout: Seq[GuaranteeTransactionCsvRow] = {
      guaranteeTransaction.dueDates.zipWithIndex.flatMap {
        case (dueDate, index) => dueDateBreakdown(dueDate, index, guaranteeTransaction.dueDates.size)
      }
    }

    private def dueDateBreakdown(dueDate: DueDate, index: Int, of: Int) = {
      val summaryRow = dueDateSummary(dueDate, index, of)
      val taxBreakdownRows = dueDate.taxTypeGroups.map(taxBreakdown)
      summaryRow +: taxBreakdownRows
    }

    private def dueDateSummary(dueDate: DueDate, index: Int, of: Int) = {
      val detailRow = GuaranteeTransactionCsvRow(
        securityReasonBreakdown = Some(messages("cf.guarantee-account.csv.security-reason", index + 1, of)),
        taxCode = Some(dueDate.reasonForSecurity.getOrElse(messages("cf.guarantee-account.csv.unknown"))),
        expiryDate = Some(dueDate.dueDate),
        lastUpdated = Some(dueDate.amounts.updateDate)
      )

      if (index == 0) {
        val summaryRow = detailRow.copy(
          date = Some(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(guaranteeTransaction.date)),
          movementReferenceNumber = Some(guaranteeTransaction.movementReferenceNumber),
          uniqueConsignmentReference = guaranteeTransaction.uniqueConsignmentReference,
          consigneeEori = Some(guaranteeTransaction.consigneeEori),
          declarantEori = Some(guaranteeTransaction.declarantEori),
          totalOriginalCharge = Some(guaranteeTransaction.originalCharge),
          totalDischargedAmount = Some(guaranteeTransaction.dischargedAmount),
          totalBalance = Some(guaranteeTransaction.balance),
          interestCharge = guaranteeTransaction.interestCharge.map(BigDecimal(_)).orElse(Some(BigDecimal(0))),
          c18Reference = guaranteeTransaction.c18Reference
        )
        summaryRow
      } else {
        detailRow
      }
    }

    private def taxBreakdown(taxTypeGroup: TaxTypeGroup) = {
      val taxType = taxTypeGroup.taxType

      GuaranteeTransactionCsvRow(
        securityReasonBreakdown = Some(s"$indentation${taxTypeGroup.taxTypeGroup}"),
        taxCode = Some(taxType.taxType),
        originalCharge = Some(BigDecimal(taxType.amounts.totalAmount)),
        dischargedAmount = taxType.amounts.clearedAmount.map(BigDecimal(_)).orElse(Some(BigDecimal(0))),
        balance = taxType.amounts.openAmount.map(BigDecimal(_)).orElse(Some(BigDecimal(0))),
        lastUpdated = Some(taxType.amounts.updateDate)
      )
    }
  }

}
