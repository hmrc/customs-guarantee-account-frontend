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

package crypto

import models._
import play.api.Configuration
import play.api.test.Helpers.running
import utils.SpecBase

import java.time.{LocalDate, Month}

class GuaranteeTransactionsEncryptionSpec extends SpecBase {

  "Encrypt and decrypt a guarantee account" in {
    val app = application.build()

    val amt = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
    val tt = TaxType("VAT", amt)
    val ttg = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt)
    val dd = DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg))

    val year = 2018
    val day = 22

    val transaction = GuaranteeTransaction(LocalDate.of(year, Month.JULY, day),
      "MRN-1",
      None,
      BigDecimal(12369.50),
      None,
      "GB30000",
      "GB40000",
      BigDecimal(32.00),
      BigDecimal(26.20),
      None,
      Some("C18-1"),
      dueDates = Seq(dd))


    val encryptor = app.injector.instanceOf[GuaranteeTransactionsEncryptor]
    val decryptor = app.injector.instanceOf[GuaranteeTransactionsDecryptor]
    val config = app.injector.instanceOf[Configuration]
    val key = config.get[String]("mongodb.encryptionKey")
    running(app) {
      val encrypted = encryptor.encryptGuaranteeTransactions(Seq(transaction), key)
      decryptor.decryptGuaranteeTransactions(encrypted, key) mustBe Seq(transaction)
    }
  }
}
