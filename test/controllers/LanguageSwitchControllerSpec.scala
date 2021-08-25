/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import models.Language
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.SpecBase

class LanguageSwitchControllerSpec extends SpecBase {

  "switching language when translation is enabled" should {

    "set the language to Cymraeg" in {

      val app: Application = application.configure("features.welsh-translation" -> true).build()

      running(app) {
        val request = FakeRequest(GET, routes.LanguageSwitchController.switchToLanguage(Language.Cymraeg).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        cookies(result).get("PLAY_LANG").value.value mustEqual "cy"
      }
    }

    "should set the language to English" in {

      val app = application.configure("features.welsh-translation" -> true).build()

      running(app) {
        val request = FakeRequest(GET, routes.LanguageSwitchController.switchToLanguage(Language.English).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        cookies(result).get("PLAY_LANG").value.value mustEqual "en"
      }
    }
  }

}
