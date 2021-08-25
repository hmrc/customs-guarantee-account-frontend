/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import config.AppConfig
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject

class LogoutController @Inject()(
                                  appConfig: AppConfig,
                                  mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  def logout: Action[AnyContent] = Action {
    Redirect(appConfig.signOutUrl, Map("continue" -> Seq(appConfig.feedbackService)))
  }

  def logoutNoSurvey: Action[AnyContent] = Action {
    Redirect(appConfig.signOutUrl)
  }
}
