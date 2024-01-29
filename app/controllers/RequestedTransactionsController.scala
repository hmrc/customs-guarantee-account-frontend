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

import cats.data.EitherT
import cats.data.EitherT.fromOptionF
import config.{AppConfig, ErrorHandler}
import connectors._
import controllers.actions.{IdentifierAction, EmailAction}
import models.GuaranteeAccount
import models.request.IdentifierRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.RequestedTransactionsCache
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.{GuaranteeAccountViewModel, ResultsPageSummary}
import views.html._

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestedTransactionsController @Inject()(apiConnector: CustomsFinancialsApiConnector,
                                                identify: IdentifierAction,
                                                checkEmailIsVerified: EmailAction,
                                                dateTimeService: DateTimeService,
                                                cache: RequestedTransactionsCache,
                                                tooManyResults: guarantee_transactions_too_many_results,
                                                noResults: guarantee_transactions_no_result,
                                                guaranteeAccountTransactionsNotAvailable: guarantee_account_transactions_not_available,
                                                resultView: guarantee_transactions_result_page,
                                                eh: ErrorHandler,
                                                mcc: MessagesControllerComponents)(
                                                implicit executionContext: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen checkEmailIsVerified).async { implicit request =>

    val result: EitherT[Future, Result, Result] = for {
      dates <- fromOptionF(cache.get(request.eori), Redirect(routes.RequestTransactionsController.onPageLoad()))
      account <- fromOptionF(apiConnector.getGuaranteeAccount(request.eori), NotFound(eh.notFoundTemplate))
      page <- EitherT.liftF(showAccountWithTransactionDetails(account, dates.start, dates.end))
    } yield page

    result.merge.recover { case e =>
      logger.error(s"Unable to retrieve account details: ${e.getMessage}")
      Redirect(routes.GuaranteeAccountController.showAccountUnavailable)
    }
  }

  private def showAccountWithTransactionDetails(account: GuaranteeAccount, from: LocalDate, to: LocalDate)(
    implicit req: IdentifierRequest[AnyContent]): Future[Result] = {
    apiConnector.retrieveRequestedGuaranteeTransactionsDetail(account.number, onlyOpenItems = true, from, to).map {

      case Left(error) => error match {
        case NoTransactionsAvailable => Ok(noResults(new ResultsPageSummary(from, to)))

        case TooManyTransactionsRequested => Ok(tooManyResults(
          new ResultsPageSummary(from, to), controllers.routes.RequestTransactionsController.onPageLoad().url))

        case UnknownException => Ok(guaranteeAccountTransactionsNotAvailable(
          GuaranteeAccountViewModel(account, dateTimeService.localDateTime())))
      }
      case Right(_) => Ok(resultView(
        new ResultsPageSummary(from, to),
        controllers.routes.GuaranteeAccountController.showAccountDetails(None).url))
    }
  }
}
