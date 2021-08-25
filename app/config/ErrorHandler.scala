/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package config

import play.api.i18n.MessagesApi
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.{ErrorTemplate, not_found}

import javax.inject.{Inject, Singleton}

@Singleton
class ErrorHandler @Inject()(errorTemplate: ErrorTemplate, notFound: not_found, val messagesApi: MessagesApi)(implicit appConfig: AppConfig)
  extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
    errorTemplate(pageTitle, heading, message)

  def customNotFound()(implicit request: Request[_]): Html =
    notFound()
}
