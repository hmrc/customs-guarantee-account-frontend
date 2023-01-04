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
