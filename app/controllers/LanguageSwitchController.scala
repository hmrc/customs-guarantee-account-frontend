/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import com.google.inject.Inject
import models.Language
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

class LanguageSwitchController @Inject()(
                                          override implicit val messagesApi: MessagesApi,
                                          val controllerComponents: MessagesControllerComponents
                                        ) extends FrontendBaseController with I18nSupport {

  private def fallbackURL: String = routes.GuaranteeAccountController.showAccountDetails(None).url

  def switchToLanguage(language: Language): Action[AnyContent] = Action {
    implicit request =>
      val redirectURL = request.headers.get(REFERER).getOrElse(fallbackURL)
      Redirect(redirectURL).withLang(language.lang)
  }
}
