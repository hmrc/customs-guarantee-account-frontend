/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package crypto

import com.typesafe.config.Config
import uk.gov.hmrc.crypto.{Crypted, CryptoGCMWithKeysFromConfig, PlainText}
import java.net.{URLDecoder, URLEncoder}
import javax.inject.Inject

class UrlEncryption @Inject()(config: Config) {
  val crypto = new CryptoGCMWithKeysFromConfig("mrn-encryption", config: Config)
  def encrypt(value: String) = {
    val encryptedValue = crypto.encrypt(PlainText(value)).value
    URLEncoder.encode(encryptedValue, "UTF8")
  }

  def decrypt(value: String): String = {
    val decodedValue = URLDecoder.decode(value, "UTF8")
    crypto.decrypt(Crypted(decodedValue)).value
  }
}
