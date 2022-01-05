/*
 * Copyright 2022 HM Revenue & Customs
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

import models._
import play.api.i18n.Messages
import play.api.test.Helpers
import utils.SpecBase

import java.time.LocalDate

class GuaranteeTransactionCsvRowSpec extends SpecBase {

  import viewmodels.GuaranteeTransactionCsvRow._

  implicit val messages: Messages = Helpers.stubMessages()

  val amt = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
  val tt = TaxType("VAT", amt)
  val ttg = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt)
  val dd = DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg))

  val ttAmounts1 = Amounts("25.00", Some("35.00"), None, "2020-08-02")
  val ttAmounts2 = Amounts("60.00", None, Some("50.00"), "2020-08-03")
  val tt1 = TaxType("A00", ttAmounts1)
  val tt2 = TaxType("B00", ttAmounts2)
  val ttg1 = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt1)
  val ttg2 = TaxTypeGroup(taxTypeGroup = "Duty", amounts = amt, taxType = tt2)

  val domainTransaction = GuaranteeTransaction(
    LocalDate.parse("2020-03-01"),
    "mrn0",
    None,
    100.00,
    Some("ucr1"),
    "Declarant EORI1",
    "Consignee EORI2",
    21.00,
    11.00,
    interestCharge = Some("0.1"),
    c18Reference = Some("c18ref"),
    dueDates = Seq(dd)
  )

  "GuaranteeTransactionCsvRow" should {
    "generate a summary row" in {

      val formattedData = domainTransaction.toReportLayout

      formattedData.head must be(GuaranteeTransactionCsvRow(
        date = Some("2020-03-01"),
        movementReferenceNumber = Some("mrn0"),
        uniqueConsignmentReference = Some("ucr1"),
        consigneeEori = Some("Consignee EORI2"),
        declarantEori = Some("Declarant EORI1"),
        totalOriginalCharge = Some(21.00),
        totalDischargedAmount = Some(11.00),
        totalBalance = Some(100.00),
        interestCharge = Some(0.1),
        c18Reference = Some("c18ref"),
        securityReasonBreakdown = Some("cf.guarantee-account.csv.security-reason"),
        taxCode = Some("T24"),
        expiryDate = Some("2020-07-28"),
        lastUpdated = Some("2020-08-01")
      ))
    }

    "default missing interest charge to zero" in {
      val transactionWithNoInterest = domainTransaction.copy(interestCharge = None)

      val formattedData = transactionWithNoInterest.toReportLayout

      formattedData.head.interestCharge must be(Some(0.0))
    }

    "generate detail rows for each tax type group" in {
      val multipleTaxTypeGroups = domainTransaction.copy(dueDates = Seq(DueDate(
        dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg1, ttg2))))

      val formattedData = multipleTaxTypeGroups.toReportLayout

      formattedData.tail must be(Seq(
        GuaranteeTransactionCsvRow(
          securityReasonBreakdown = Some("   VAT"),
          taxCode = Some("A00"),
          originalCharge = Some(25.0),
          dischargedAmount = Some(35.0),
          balance = Some(0.0),
          lastUpdated = Some("2020-08-02")
        ),
        GuaranteeTransactionCsvRow(
          securityReasonBreakdown = Some("   Duty"),
          taxCode = Some("B00"),
          originalCharge = Some(60.0),
          dischargedAmount = Some(0.0),
          balance = Some(50.0),
          lastUpdated = Some("2020-08-03")
        )))
    }

    "generate a set of detail rows per security reason" in {
      val multipleSecurityReasons = domainTransaction.copy(dueDates = Seq(
        DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg1, ttg2)),
        DueDate(dueDate = "2020-07-29", reasonForSecurity = Some("T25"), amounts = amt, taxTypeGroups = Seq(ttg2, ttg1))
      ))

      multipleSecurityReasons.toReportLayout must be(Seq(
        GuaranteeTransactionCsvRow(
          date = Some("2020-03-01"),
          movementReferenceNumber = Some("mrn0"),
          uniqueConsignmentReference = Some("ucr1"),
          consigneeEori = Some("Consignee EORI2"),
          declarantEori = Some("Declarant EORI1"),
          totalOriginalCharge = Some(21.00),
          totalDischargedAmount = Some(11.00),
          totalBalance = Some(100.00),
          interestCharge = Some(0.1),
          c18Reference = Some("c18ref"),
          securityReasonBreakdown = Some("cf.guarantee-account.csv.security-reason"),
          taxCode = Some("T24"),
          expiryDate = Some("2020-07-28"),
          lastUpdated = Some("2020-08-01")
        ),
        GuaranteeTransactionCsvRow(
          securityReasonBreakdown = Some("   VAT"),
          taxCode = Some("A00"),
          originalCharge = Some(25.0),
          dischargedAmount = Some(35.0),
          balance = Some(0.0),
          lastUpdated = Some("2020-08-02")
        ),
        GuaranteeTransactionCsvRow(
          securityReasonBreakdown = Some("   Duty"),
          taxCode = Some("B00"),
          originalCharge = Some(60.0),
          dischargedAmount = Some(0.0),
          balance = Some(50.0),
          lastUpdated = Some("2020-08-03")
        ),
        GuaranteeTransactionCsvRow(
          securityReasonBreakdown = Some("cf.guarantee-account.csv.security-reason"),
          taxCode = Some("T25"),
          expiryDate = Some("2020-07-29"),
          lastUpdated = Some("2020-08-01")
        ),
        GuaranteeTransactionCsvRow(
          securityReasonBreakdown = Some("   Duty"),
          taxCode = Some("B00"),
          originalCharge = Some(60.0),
          dischargedAmount = Some(0.0),
          balance = Some(50.0),
          lastUpdated = Some("2020-08-03")
        ),
        GuaranteeTransactionCsvRow(
          securityReasonBreakdown = Some("   VAT"),
          taxCode = Some("A00"),
          originalCharge = Some(25.0),
          dischargedAmount = Some(35.0),
          balance = Some(0.0),
          lastUpdated = Some("2020-08-02")
        )
      ))
    }

    "default missing reason for security to 'Unknown'" in {
      val transactionWithNoSecurityReason = domainTransaction.copy(dueDates = Seq(dd.copy(reasonForSecurity = None)))

      val formattedData = transactionWithNoSecurityReason.toReportLayout

      formattedData.head.taxCode must be(Some("cf.guarantee-account.csv.unknown"))
    }
  }
}
