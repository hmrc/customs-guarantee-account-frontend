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

import models.Language.{Cymraeg, English}
import utils.SpecBase

class LanguageSpec extends SpecBase {

  "pathBindable" should {

    import Language.pathBindable

    "bind the value correctly" in {
      pathBindable.bind("language", Cymraeg.toString) mustBe Right(Cymraeg)
      pathBindable.bind("language", English.toString) mustBe Right(English)
    }

    "throw exception when unsupported language value is bound" in {
      intercept[RuntimeException] {
        pathBindable.bind("language", "Unknown")
      }.getMessage mustBe "Language Unknown is not supported"

    }

    "unbind the value correctly" in {
      pathBindable.unbind("language", Cymraeg) mustBe Cymraeg.toString
      pathBindable.unbind("language", English) mustBe English.toString
    }
  }
}
