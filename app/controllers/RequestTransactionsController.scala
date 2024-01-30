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

import controllers.actions.{IdentifierAction, EmailAction}
import forms.GuaranteeTransactionsRequestPageFormProvider
import models.GuaranteeTransactionDates
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html._
import utils.Utils.{singleSpace, hyphen, comma}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestTransactionsController @Inject()(identify: IdentifierAction,
                                              checkEmailIsVerified: EmailAction,
                                              formProvider: GuaranteeTransactionsRequestPageFormProvider,
                                              view: guarantee_transactions_request_page,
                                              cache: RequestedTransactionsCache,
                                              implicit val mcc: MessagesControllerComponents)
                                             (implicit ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  val log: Logger = Logger(this.getClass)

  def form: Form[GuaranteeTransactionDates] = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen checkEmailIsVerified).async {
    implicit request => for {
      _ <- cache.clear(request.eori)
    } yield Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = identify.async {
    implicit request =>
      form.bindFromRequest().fold(formWithErrors => {
        logMessageForAnalytics(request.eori, formWithErrors)
        Future.successful(BadRequest(view(formWithErrors)))
      },
          value => customValidation(value, form) match {
            case Some(formWithErrors) =>
              logMessageForAnalytics(request.eori, formWithErrors)
              Future.successful(BadRequest(view(formWithErrors)))
            case None =>
              cache.set(request.eori, value).map { _ =>
                Redirect(routes.RequestedTransactionsController.onPageLoad())
              }
          }
      )
  }

  private def customValidation(dates: GuaranteeTransactionDates, form: Form[GuaranteeTransactionDates]): Option[Form[GuaranteeTransactionDates]] = {
    def populateErrors(startMessage: String, endMessage: String): Form[GuaranteeTransactionDates] = {
      form.withError("start", startMessage)
        .withError("end", endMessage).fill(dates)
    }

    dates match {
      case GuaranteeTransactionDates(start, end) if start.isAfter(end) =>
        Some(populateErrors("cf.form.error.start-after-end", "cf.form.error.end-before-start"))
      case _ => None
    }
  }

  private def logMessageForAnalytics(eori: String, formWithErrors: Form[GuaranteeTransactionDates])
                                    (implicit messages: Messages): Unit= {
    val errorMessages = formWithErrors.errors.map(e => messages(e.message)).mkString(comma)

    val startDate = formWithErrors.data.getOrElse("start.year", singleSpace) + hyphen +
      formWithErrors.data.getOrElse("start.month", singleSpace)

    val endDate = formWithErrors.data.getOrElse("end.year", singleSpace) + hyphen +
      formWithErrors.data.getOrElse("end.month", singleSpace)

    log.warn(s"Guarantee account, transaction request service, eori number: $eori, " +
      s"start date: $startDate, end date: $endDate, error: $errorMessages")
  }
}
