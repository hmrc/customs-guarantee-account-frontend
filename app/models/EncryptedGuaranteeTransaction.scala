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

package models

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.crypto.Crypted

import java.time.LocalDate

case class EncryptedAmounts(
  totalAmount: Crypted,
  clearedAmount: Option[Crypted],
  openAmount: Option[Crypted],
  updateDate: Crypted
)

case class EncryptedDueDate(
  dueDate: Crypted,
  reasonForSecurity: Option[Crypted],
  amounts: EncryptedAmounts,
  taxTypeGroups: Seq[EncryptedTaxTypeGroup]
)

case class EncryptedTaxTypeGroup(
  taxTypeGroup: Crypted,
  amounts: EncryptedAmounts,
  taxType: EncryptedTaxType
)

case class EncryptedTaxType(taxType: Crypted, amounts: EncryptedAmounts)

case class EncryptedGuaranteeTransaction(
  date: LocalDate,
  movementReferenceNumber: Crypted,
  secureMovementReferenceNumber: Option[String],
  balance: Crypted,
  uniqueConsignmentReference: Option[Crypted],
  declarantEori: Crypted,
  consigneeEori: Crypted,
  originalCharge: Crypted,
  dischargedAmount: Crypted,
  interestCharge: Option[Crypted],
  c18Reference: Option[Crypted],
  dueDates: Seq[EncryptedDueDate]
)

object EncryptedGuaranteeTransaction {
  import crypto.Crypted.format

  implicit val amountFormat: OFormat[EncryptedAmounts]            = Json.format[EncryptedAmounts]
  implicit val taxTypeFormat: OFormat[EncryptedTaxType]           = Json.format[EncryptedTaxType]
  implicit val taxTypeGroupFormat: OFormat[EncryptedTaxTypeGroup] = Json.format[EncryptedTaxTypeGroup]
  implicit val dueDateFormat: OFormat[EncryptedDueDate]           = Json.format[EncryptedDueDate]
  implicit val format: OFormat[EncryptedGuaranteeTransaction]     = Json.format[EncryptedGuaranteeTransaction]
}
