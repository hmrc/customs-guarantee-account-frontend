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
import play.api.libs.json.Json

import java.time.LocalDate

case class EncryptedAmounts(totalAmount: EncryptedValue,
                            clearedAmount: Option[EncryptedValue],
                            openAmount: Option[EncryptedValue],
                            updateDate: EncryptedValue)

case class EncryptedDueDate(dueDate: EncryptedValue,
                            reasonForSecurity: Option[EncryptedValue],
                            amounts: EncryptedAmounts,
                            taxTypeGroups: Seq[EncryptedTaxTypeGroup])

case class EncryptedTaxTypeGroup(taxTypeGroup: EncryptedValue,
                                 amounts: EncryptedAmounts,
                                 taxType: EncryptedTaxType)

case class EncryptedTaxType(taxType: EncryptedValue,
                            amounts: EncryptedAmounts)

case class EncryptedGuaranteeTransaction(date: LocalDate,
                                         movementReferenceNumber: EncryptedValue,
                                         secureMovementReferenceNumber: Option[String],
                                         balance: EncryptedValue,
                                         uniqueConsignmentReference: Option[EncryptedValue],
                                         declarantEori: EncryptedValue,
                                         consigneeEori: EncryptedValue,
                                         originalCharge: EncryptedValue,
                                         dischargedAmount: EncryptedValue,
                                         interestCharge: Option[EncryptedValue],
                                         c18Reference: Option[EncryptedValue],
                                         dueDates: Seq[EncryptedDueDate])

object EncryptedGuaranteeTransaction {
  implicit val amountFormat = Json.format[EncryptedAmounts]
  implicit val taxTypeFormat = Json.format[EncryptedTaxType]
  implicit val taxTypeGroupFormat = Json.format[EncryptedTaxTypeGroup]
  implicit val dueDateFormat = Json.format[EncryptedDueDate]
  implicit val format = Json.format[EncryptedGuaranteeTransaction]
}
