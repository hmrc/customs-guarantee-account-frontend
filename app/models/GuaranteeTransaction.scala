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

package models

import models.domain.EORI
import play.api.libs.functional.syntax._
import play.api.libs.json._
import viewmodels.{CSVWritable, FieldNames}

import java.time.LocalDate


case class Amounts(totalAmount: String, clearedAmount: Option[String], openAmount: Option[String], updateDate: String)

case class DueDate(dueDate: String, reasonForSecurity: Option[String], amounts: Amounts, taxTypeGroups: Seq[TaxTypeGroup]) {
  def securityReason: Option[SecurityReason] = {
    (reasonForSecurity, taxTypeGroups) match {
      case (_, taxTypeGroups) if taxTypeGroups.isEmpty => None
      case (Some(taxCode), taxTypeGroups) => Some(SecurityReason(taxCode, taxTypeGroups))
      case _ => None
    }
  }
}

case class TaxTypeGroup(taxTypeGroup: String, amounts: Amounts, taxType: TaxType)

case class TaxType(taxType: String, amounts: Amounts)

case class GuaranteeTransaction(
                                 date: LocalDate,
                                 movementReferenceNumber: String,
                                 secureMovementReferenceNumber: Option[String],
                                 balance: BigDecimal,
                                 uniqueConsignmentReference: Option[String],
                                 declarantEori: EORI,
                                 consigneeEori: EORI,
                                 originalCharge: BigDecimal,
                                 dischargedAmount: BigDecimal,
                                 interestCharge: Option[String],
                                 c18Reference: Option[String],
                                 dueDates: Seq[DueDate]
                               ) extends CSVWritable with FieldNames {
  def moreThanOne: Boolean = dueDates.size > 1

  override def fieldNames: Seq[String] = Seq(
    "date",
    "movementReferenceNumber",
    "secureMovementReferenceNumber",
    "balance",
    "uniqueConsignmentReference",
    "declarantEori",
    "consigneeEori",
    "originalCharge",
    "dischargedAmount",
    "interestCharge",
    "c18Reference",
    "dueDates"
  )
}


case class GuaranteeAccountTransaction(guaranteeTransaction: GuaranteeTransaction, c18References: Seq[GuaranteeTransaction])


object GuaranteeTransaction {

  implicit val amountReads = Json.reads[Amounts]

  implicit val taxTypeReads = Json.reads[TaxType]

  implicit val taxTypeGroupReads = Json.reads[TaxTypeGroup]

  implicit val dueDateReads = Json.reads[DueDate]

  implicit val guaranteeTransactionReads: Reads[GuaranteeTransaction] = {
    val defaultToZero = Reads.pure(BigDecimal(0))
    (
      (__ \ "date").read[LocalDate] and
        (__ \ "movementReferenceNumber").read[String] and
        (__ \ "secureMovementReferenceNumber").readNullable[String] and
        ((__ \ "balance").read[BigDecimal] or defaultToZero) and
        (__ \ "uniqueConsignmentReference").readNullable[String] and
        (__ \ "declarantEori").read[String] and
        (__ \ "consigneeEori").read[String] and
        (__ \ "originalCharge").read[BigDecimal] and
        ((__ \ "dischargedAmount").read[BigDecimal] or defaultToZero) and
        (__ \ "interestCharge").readNullable[String] and
        (__ \ "c18Reference").readNullable[String] and
        (__ \ "dueDates").read[Seq[DueDate]]
      ) (GuaranteeTransaction.apply _)
  }

}

case class SecurityReason(taxCode: String, taxTypeGroups: Seq[TaxTypeGroup])

