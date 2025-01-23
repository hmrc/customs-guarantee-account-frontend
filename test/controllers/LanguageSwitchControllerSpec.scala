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

package controllers

import models.Language
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.SpecBase

class LanguageSwitchControllerSpec extends SpecBase {

  "switching language when translation is enabled" should {

    "set the language to Cymraeg" in {
      val application: Application = applicationBuilder.configure("features.welsh-translation" -> true).build()

      running(application) {
        val request = FakeRequest(GET, routes.LanguageSwitchController.switchToLanguage(Language.Cymraeg).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        cookies(result).get("PLAY_LANG").value.value mustEqual "cy"
      }
    }

    "should set the language to English" in {
      val application: Application = applicationBuilder.configure("features.welsh-translation" -> true).build()

      running(application) {
        val request = FakeRequest(GET, routes.LanguageSwitchController.switchToLanguage(Language.English).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        cookies(result).get("PLAY_LANG").value.value mustEqual "en"
      }
    }
  }

}
