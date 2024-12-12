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

import config.AppConfig
import connectors.{
  CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested, UnknownException
}
import controllers.actions.{EmailAction, IdentifierAction}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.{GuaranteeAccountTransactionsViewModel, GuaranteeAccountViewModel}
import views.html.{guarantee_account, guarantee_account_exceed_threshold, individual_transaction, not_found}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeTransactionController @Inject() (
  identify: IdentifierAction,
  checkEmailIsVerified: EmailAction,
  apiConnector: CustomsFinancialsApiConnector,
  tooManyResults: guarantee_account_exceed_threshold,
  guaranteeAccount: guarantee_account,
  notFound: not_found,
  dateTimeService: DateTimeService,
  view: individual_transaction,
  mcc: MessagesControllerComponents
)(implicit execution: ExecutionContext, val appConfig: AppConfig)
    extends FrontendController(mcc)
    with I18nSupport {

  def displayTransaction(ref: String, page: Option[Int]): Action[AnyContent] =
    (identify andThen checkEmailIsVerified).async { implicit request =>
      apiConnector.getGuaranteeAccount(request.eori).flatMap {
        case None          => Future.successful(NotFound(notFound()))
        case Some(account) =>
          apiConnector.retrieveOpenGuaranteeTransactionsDetail(account.number).map {
            case Left(value)         =>
              value match {
                case NoTransactionsAvailable =>
                  Ok(
                    guaranteeAccount(
                      GuaranteeAccountViewModel(account, dateTimeService.localDateTime()),
                      GuaranteeAccountTransactionsViewModel(Seq.empty, page)
                    )
                  )

                case TooManyTransactionsRequested =>
                  Ok(tooManyResults(GuaranteeAccountViewModel(account, dateTimeService.localDateTime())))

                case UnknownException => Redirect(routes.GuaranteeAccountController.showTransactionsUnavailable())
              }
            case Right(transactions) =>
              transactions.find(_.secureMovementReferenceNumber.contains(ref)) match {
                case Some(value) =>
                  Ok(view(GuaranteeAccountViewModel(account, dateTimeService.localDateTime()), value, page))

                case None =>
                  Ok(
                    guaranteeAccount(
                      GuaranteeAccountViewModel(account, dateTimeService.localDateTime()),
                      GuaranteeAccountTransactionsViewModel(Seq.empty, page)
                    )
                  )
              }
          }
      }
    }
}
