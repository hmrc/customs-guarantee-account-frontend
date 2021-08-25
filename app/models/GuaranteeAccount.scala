/*
 * Copyright 2021 HM Revenue & Customs
 *
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


