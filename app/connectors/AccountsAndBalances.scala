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

package connectors

import models._
import play.api.libs.json._

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.util.Random

case class AccountsAndBalancesResponseContainer(accountsAndBalancesResponse: AccountsAndBalancesResponse) {
  def toGuaranteeAccounts: Seq[GuaranteeAccount] = {
    List(
      accountsAndBalancesResponse.responseDetail.generalGuaranteeAccount.map(_.map(_.toDomain()))
    ).flatten.flatten
  }
}

case class AccountsAndBalancesRequestContainer(accountsAndBalancesRequest: AccountsAndBalancesRequest)

case class AccountsRequestCommon(PID: Option[String],
                                 originatingSystem: Option[String],
                                 receiptDate: String,
                                 acknowledgementReference: String,
                                 regime: String)

object AccountsRequestCommon {
  private val MDG_ACK_REF_LENGTH = 32

  def generate(): AccountsRequestCommon = {
    val isoLocalDateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS))
    val acknowledgmentRef = generateStringOfRandomDigits(MDG_ACK_REF_LENGTH)
    val regime = "CDS"

    AccountsRequestCommon(None, None, isoLocalDateTime, acknowledgmentRef, regime)
  }

  val randomIntLimit = 10

  private def generateStringOfRandomDigits(length: Int) = {
    (1 to length).map(_ => Random.nextInt(randomIntLimit)).mkString
  }
}

case class AccountsRequestDetail(EORINo: String,
                                 accountType: Option[String],
                                 accountNumber: Option[String],referenceDate: Option[String])

case class AccountsAndBalancesRequest(requestCommon: AccountsRequestCommon,
                                      requestDetail: AccountsRequestDetail)

case class AccountsAndBalancesResponse(responseCommon: Option[AccountResponseCommon],
                                       responseDetail: AccountResponseDetail)

case class AccountResponseCommon(status: String,
                                 statusText: Option[String],
                                 processingDate: String,
                                 returnParameters: Option[Seq[ReturnParameters]])

case class ReturnParameters(paramName: String, paramValue: String)

case class AccountResponseDetail(EORINo: Option[String],
                                 referenceDate: Option[String],
                                 generalGuaranteeAccount: Option[Seq[GeneralGuaranteeAccount]])

case class GeneralGuaranteeAccount(account: Account,
                                   guaranteeLimit: Option[String], availableGuaranteeBalance: Option[String]) {
  def toDomain(): GuaranteeAccount = {
    val balance = (guaranteeLimit, availableGuaranteeBalance) match {
      case (Some(limit), Some(guarantee)) => Some(GeneralGuaranteeBalance(BigDecimal(limit), BigDecimal(guarantee)))
      case (None, Some(guarantee)) => Some(GeneralGuaranteeBalance(BigDecimal(0), BigDecimal(guarantee)))
      case _ => None
    }
    GuaranteeAccount(account.number, account.owner, account.accountStatus.getOrElse(AccountStatusOpen), balance)
  }
}

sealed trait CDSAccountStatus

case object AccountStatusOpen extends CDSAccountStatus

case object AccountStatusClosed extends CDSAccountStatus

case object AccountStatusSuspended extends CDSAccountStatus

object CDSAccountStatus {

  import play.api.Logger

  val logger = Logger(this.getClass)

  implicit val CDSAccountStatusReads = new Reads[CDSAccountStatus] {
    override def reads(json: JsValue): JsResult[CDSAccountStatus] = {
      json.as[String] match {
        case status if status.equalsIgnoreCase("Open") => JsSuccess(AccountStatusOpen)
        case status if status.equalsIgnoreCase("Suspended") => JsSuccess(AccountStatusSuspended)
        case status if status.equalsIgnoreCase("Closed") => JsSuccess(AccountStatusClosed)
        case unknown => logger.warn(s"Invalid account status: $unknown"); JsSuccess(AccountStatusOpen)
      }
    }
  }

  implicit val CDSAccountStatusWrites = new Writes[CDSAccountStatus] {
    override def writes(o: CDSAccountStatus): JsValue = {
      o match {
        case AccountStatusOpen => JsString("Open")
        case AccountStatusClosed => JsString("Suspended")
        case AccountStatusSuspended => JsString("Closed")
      }
    }
  }
}

case class Account(number: String,
                   `type`: String,
                   owner: String,
                   accountStatus: Option[CDSAccountStatus] = None,
                   viewBalanceIsGranted: Boolean = false,
                   isleOfManFlag: Option[Boolean] = None)

case class Limits(periodGuaranteeLimit: String, periodAccountLimit: String)

case class DefermentBalances(periodAvailableGuaranteeBalance: String, periodAvailableAccountBalance: String)

object AccountsAndBalancesResponseContainer {
  implicit val returnParametersReads = Json.reads[ReturnParameters]
  implicit val accountReads = Json.reads[Account]
  implicit val limitsReads = Json.reads[Limits]
  implicit val balancesReads = Json.reads[DefermentBalances]
  implicit val generalGuaranteeAccountReads = Json.reads[GeneralGuaranteeAccount]
  implicit val accountResponseDetailReads = Json.reads[AccountResponseDetail]
  implicit val accountResponseCommonReads = Json.reads[AccountResponseCommon]
  implicit val accountsAndBalancesResponseReads = Json.reads[AccountsAndBalancesResponse]
  implicit val accountsAndBalancesResponseContainerReads = Json.reads[AccountsAndBalancesResponseContainer]
}

object AccountsAndBalancesRequestContainer {
  implicit val accountsRequestCommonFormat = Json.format[AccountsRequestCommon]
  implicit val accountsRequestDetailFormat = Json.format[AccountsRequestDetail]
  implicit val accountsAndBalancesRequestFormat = Json.format[AccountsAndBalancesRequest]
  implicit val accountsAndBalancesRequestContainerFormat = Json.format[AccountsAndBalancesRequestContainer]
}
