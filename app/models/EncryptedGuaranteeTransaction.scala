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

import crypto.EncryptedValue
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.crypto.Crypted

import java.time.LocalDate

case class EncryptedAmounts(
  totalAmount: Either[EncryptedValue, Crypted],
  clearedAmount: Option[Either[EncryptedValue, Crypted]],
  openAmount: Option[Either[EncryptedValue, Crypted]],
  updateDate: Either[EncryptedValue, Crypted]
)

case class EncryptedDueDate(
  dueDate: Either[EncryptedValue, Crypted],
  reasonForSecurity: Option[Either[EncryptedValue, Crypted]],
  amounts: EncryptedAmounts,
  taxTypeGroups: Seq[EncryptedTaxTypeGroup]
)

case class EncryptedTaxTypeGroup(
  taxTypeGroup: Either[EncryptedValue, Crypted],
  amounts: EncryptedAmounts,
  taxType: EncryptedTaxType
)

case class EncryptedTaxType(taxType: Either[EncryptedValue, Crypted], amounts: EncryptedAmounts)

case class EncryptedGuaranteeTransaction(
  date: LocalDate,
  movementReferenceNumber: Either[EncryptedValue, Crypted],
  secureMovementReferenceNumber: Option[String],
  balance: Either[EncryptedValue, Crypted],
  uniqueConsignmentReference: Option[Either[EncryptedValue, Crypted]],
  declarantEori: Either[EncryptedValue, Crypted],
  consigneeEori: Either[EncryptedValue, Crypted],
  originalCharge: Either[EncryptedValue, Crypted],
  dischargedAmount: Either[EncryptedValue, Crypted],
  interestCharge: Option[Either[EncryptedValue, Crypted]],
  c18Reference: Option[Either[EncryptedValue, Crypted]],
  dueDates: Seq[EncryptedDueDate]
)

object EncryptedGuaranteeTransaction {
  import crypto.CryptoAdapterFormats.eitherFormat

  implicit val amountFormat: OFormat[EncryptedAmounts]            = Json.format[EncryptedAmounts]
  implicit val taxTypeFormat: OFormat[EncryptedTaxType]           = Json.format[EncryptedTaxType]
  implicit val taxTypeGroupFormat: OFormat[EncryptedTaxTypeGroup] = Json.format[EncryptedTaxTypeGroup]
  implicit val dueDateFormat: OFormat[EncryptedDueDate]           = Json.format[EncryptedDueDate]
  implicit val format: OFormat[EncryptedGuaranteeTransaction]     = Json.format[EncryptedGuaranteeTransaction]
}
