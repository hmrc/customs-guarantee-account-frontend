/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import play.api.test.Helpers._
import utils.SpecBase

class UnauthorisedControllerSpec extends SpecBase {

  "onPageLoad" must {
    "return OK" in {
      val app = application.build()

      running(app) {
        val result = route(app, fakeRequest("GET", routes.UnauthorisedController.onPageLoad().url)).value
        status(result) mustEqual OK
      }
    }
  }

}
