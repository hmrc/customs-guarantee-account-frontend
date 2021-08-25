/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package views.helpers

import connectors.{AccountStatusClosed, AccountStatusOpen, AccountStatusSuspended, CDSAccountStatus}

object HtmlHelper {

  implicit class Attribute(val status: CDSAccountStatus) {

    val statusAttribute: String = status match {
      case AccountStatusOpen => "account-status-open"
      case AccountStatusSuspended => "account-status-suspended"
      case AccountStatusClosed => "account-status-closed"
    }

    val isOpen: Boolean = status == AccountStatusOpen

    val balanceAttribute: String = status match {
      case AccountStatusOpen => "account-balance-status-open"
      case AccountStatusSuspended => "account-balance-status-suspended"
      case AccountStatusClosed => "account-balance-status-closed"
    }
  }

}
