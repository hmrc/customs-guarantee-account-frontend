/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import utils.SpecBase

class GuaranteeTransactionSpec extends SpecBase {


  val amt = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
  val tt = TaxType("VAT", amt)
  val ttg = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt)
  val dd = DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg))
  val ddNoTaxTypeGroups = DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq.empty)

  "getSecurityReason" should {
    "return None when dueDates are empty " in {
      ddNoTaxTypeGroups.securityReason mustBe None
    }
    "return security reason  when dueDates are not empty " in {
      Some(dd.securityReason.get.taxCode) mustBe dd.reasonForSecurity
    }
  }
}
