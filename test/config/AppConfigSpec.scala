/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package config

import play.api.test.Helpers.running
import utils.SpecBase

class AppConfigSpec extends SpecBase {

  "AppConfig" should {
    "include the app name" in new Setup {
      running(app) {
        appConfig.appName mustBe ("customs-guarantee-account-frontend")
      }
    }

  }

  trait Setup {
    val app = application.build()
    val appConfig = app.injector.instanceOf[AppConfig]
  }
}
