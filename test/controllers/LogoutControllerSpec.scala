/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.SpecBase

class LogoutControllerSpec extends SpecBase {

  "logout" must {
    "redirect the user to logout with the continue as the feedback survey url" in {
      val app = application
        .configure("feedback.url" -> "/some-continue", "feedback.source" -> "/CDS-FIN")
        .build()
      running(app) {
        val request = FakeRequest(GET, routes.LogoutController.logout().url)

        val result = route(app, request).value
        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/sign-out-without-state?continue=%2Fsome-continue%2FCDS-FIN"
      }
    }
  }

  "logoutNoSurvey" must {
    "redirect the user to logout with no continue location" in {
      val app = application
        .configure("feedback.url" -> "/some-continue", "feedback.source" -> "/CDS-FIN")
        .build()
      running(app) {
        val request = FakeRequest(GET, routes.LogoutController.logoutNoSurvey().url)

        val result = route(app, request).value
        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/sign-out-without-state"
      }
    }
  }
}
