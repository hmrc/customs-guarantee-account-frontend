/*
 * Copyright 2021 HM Revenue & Customs
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

import config.{AppConfig, ErrorHandler}
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested, UnknownException}
import controllers.actions.IdentifierAction
import forms.GuaranteeTransactionsRequestPageFormProvider
import models.request.IdentifierRequest
import models.{GuaranteeAccount, GuaranteeTransactionDates}
import org.slf4j.LoggerFactory
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels._
import views.html._

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class GuaranteeTransactionsRequestPageController @Inject()(
                                                            identify: IdentifierAction,
                                                            formProvider: GuaranteeTransactionsRequestPageFormProvider,
                                                            view: guarantee_transactions_request_page,
                                                            apiConnector: CustomsFinancialsApiConnector,
                                                            tooManyResults: guarantee_transactions_too_many_results,
                                                            noResults: guarantee_transactions_no_result,
                                                            guaranteeAccountTransactionsNotAvailable: guarantee_account_transactions_not_available,
                                                            resultView: guarantee_transactions_result_page,
                                                            dateTimeService: DateTimeService,
                                                            implicit val mcc: MessagesControllerComponents)
                                                          (implicit ec: ExecutionContext,
                                                           eh: ErrorHandler,
                                                           appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  def form: Form[GuaranteeTransactionDates] = formProvider()

  def onPageLoad(): Action[AnyContent] = identify {
    implicit request => Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = identify.async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        value =>
          customValidation(value, form) match {
            case Some(formWithErrors) =>
              Future.successful(BadRequest(view(formWithErrors)))
            case None =>
              val result = for {
                account <- apiConnector.getGuaranteeAccount(request.eori)
                (from, to) = (value.start, value.end)
                page <- account match {
                  case Some(value) => showAccountWithTransactionDetails(value, from, to)
                  case None => Future.successful(NotFound(eh.notFoundTemplate))
                }
              } yield page

              result.recover {
                case NonFatal(t) =>
                  logger.error(s"Unable to retrieve account details: ${
                    t.getMessage
                  }")
                  Redirect(routes.GuaranteeAccountController.showAccountUnavailable())
              }
          }
      )
  }

  private def showAccountWithTransactionDetails(account: GuaranteeAccount, from: LocalDate, to: LocalDate)(implicit req: IdentifierRequest[AnyContent]): Future[Result] = {
    apiConnector.retrieveRequestedGuaranteeTransactionsDetail(account.number, onlyOpenItems = true, from, to).map {
      case Left(error) => error match {
        case NoTransactionsAvailable => Ok(noResults(new ResultsPageSummary(from, to)))
        case TooManyTransactionsRequested => Ok(tooManyResults(new ResultsPageSummary(from, to), controllers.routes.GuaranteeTransactionsRequestPageController.onPageLoad().url))
        case UnknownException => Ok(guaranteeAccountTransactionsNotAvailable(GuaranteeAccountViewModel(account, dateTimeService.localDateTime())))
      }
      case Right(_) => Ok(resultView(new ResultsPageSummary(from, to), controllers.routes.GuaranteeAccountController.showAccountDetails(None).url))
    }
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
}
