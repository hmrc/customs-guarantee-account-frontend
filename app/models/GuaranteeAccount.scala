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

import connectors.CDSAccountStatus
import crypto.EncryptedValue
import play.api.libs.json.{Json, OFormat}

import scala.math.Numeric.BigDecimalIsFractional.zero


trait Balances

case class GuaranteeAccount(number: String,
                            owner: String,
                            status: CDSAccountStatus,
                            balances: Option[GeneralGuaranteeBalance]
                           )

case class EncryptedGuaranteeAccount(number: EncryptedValue,
                                     owner: EncryptedValue,
                                     status: CDSAccountStatus,
                                     balances: Option[GeneralGuaranteeBalance])

object EncryptedGuaranteeAccount {
  implicit val format: OFormat[EncryptedGuaranteeAccount] = Json.format[EncryptedGuaranteeAccount]
}

case class GeneralGuaranteeBalance(GuaranteeLimit: BigDecimal,
                                   AvailableGuaranteeBalance: BigDecimal) extends Balances {

  val usedFunds: BigDecimal = GuaranteeLimit - AvailableGuaranteeBalance
  val usedPercentage: BigDecimal = if (GuaranteeLimit.equals(zero)) zero else usedFunds / GuaranteeLimit * 100
}

object GeneralGuaranteeBalance {
  implicit val format: OFormat[GeneralGuaranteeBalance] = Json.format[GeneralGuaranteeBalance]
}


