/*
 * Copyright 2021 HM Revenue & Customs
 *
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

    val transaction = GuaranteeTransaction(LocalDate.of(2018, Month.JULY, 22),
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
