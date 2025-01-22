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

package views

import config.AppConfig
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import utils.SpecBase
import utils.Utils.emptyString

class ViewUtilsSpec extends SpecBase {

  "view utils" should {

    "return title as name for valid form" in new Setup {
      val validFormData = sampleForm.bind(Map("name" -> "bob", "age" -> "25"))
      val scenarioA     = ViewUtils.title(validFormData, "user details", None, Seq())(messages)

      scenarioA.trim mustEqual "user details"
    }

    "return title with error for invalid form" in new Setup {
      val inValidFormData = sampleForm.bind(Map("name" -> emptyString, "age" -> "25"))
      val scenarioB       = ViewUtils.title(inValidFormData, "user details", None, Seq())(messages)

      scenarioB mustEqual "Error: user details"
    }
  }

  trait Setup {
    val hundred = 100

    val sampleForm = Form(
      tuple(
        "name" -> nonEmptyText,
        "age"  -> number(min = 0, max = hundred)
      )
    )
  }
}
