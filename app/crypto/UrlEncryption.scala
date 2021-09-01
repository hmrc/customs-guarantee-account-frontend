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
