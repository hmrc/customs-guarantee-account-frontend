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

import cats.data.EitherT.*
import cats.instances.future.*
import config.AppConfig
import connectors.*
import controllers.actions.{EmailAction, IdentifierAction}
import helpers.DateFormatters
import models.*
import models.request.IdentifierRequest
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, *}
import repositories.RequestedTransactionsCache
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.*
import views.html.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class GuaranteeAccountController @Inject() (
  identify: IdentifierAction,
  checkEmailIsVerified: EmailAction,
  apiConnector: CustomsFinancialsApiConnector,
  dateTimeService: DateTimeService,
  cache: RequestedTransactionsCache,
  guaranteeAccount: guarantee_account,
  guaranteeAccountNotAvailable: guarantee_account_not_available,
  tooManyResults: guarantee_account_exceed_threshold,
  guaranteeAccountTransactionsNotAvailable: guarantee_account_transactions_not_available,
  notFound: not_found
)(implicit mcc: MessagesControllerComponents, ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc)
    with I18nSupport
    with DateFormatters {

  val log: Logger = Logger(this.getClass)

  def showAccountDetails(page: Option[Int]): Action[AnyContent] = (identify andThen checkEmailIsVerified).async {
    implicit request =>

      cache.clear(request.eori)

      val result = for {
        account <- fromOptionF[Future, Result, GuaranteeAccount](
                     apiConnector.getGuaranteeAccount(request.eori),
                     NotFound(notFound())
                   )

        page <- liftF[Future, Result, Result](showAccountWithTransactionDetails(account, page))
      } yield page

      result.merge.recover { case NonFatal(t) =>
        log.error(s"Unable to retrieve account details: ${t.getMessage}")
        Redirect(routes.GuaranteeAccountController.showAccountUnavailable)
      }
  }

  private def showAccountWithTransactionDetails(account: GuaranteeAccount, page: Option[Int])(implicit
    req: IdentifierRequest[AnyContent]
  ): Future[Result] =
    for {
      transactions <- apiConnector.retrieveOpenGuaranteeTransactionsDetail(account.number)
      result        = transactions match {
                        case Left(error) =>
                          error match {
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
                          val (nonC18Transactions, c18Transactions) = transactions.partition(_.c18Reference.isEmpty)
                          val filteredTransactions                  = nonC18Transactions.map { transaction =>
                            GuaranteeAccountTransaction(
                              transaction,
                              c18Transactions.filter(_.movementReferenceNumber == transaction.movementReferenceNumber)
                            )
                          }
                          Ok(
                            guaranteeAccount(
                              GuaranteeAccountViewModel(account, dateTimeService.localDateTime()),
                              GuaranteeAccountTransactionsViewModel(filteredTransactions, page)
                            )
                          )
                      }
    } yield result

  def showTransactionsUnavailable(): Action[AnyContent] = (identify andThen checkEmailIsVerified).async {
    implicit request =>

      val result = for {
        account <- fromOptionF[Future, Result, GuaranteeAccount](
                     apiConnector.getGuaranteeAccount(request.eori),
                     NotFound(notFound())
                   )
      } yield Ok(
        guaranteeAccountTransactionsNotAvailable(GuaranteeAccountViewModel(account, dateTimeService.localDateTime()))
      )

      result.merge.recover { case NonFatal(e) =>
        log.error(s"Unable to retrieve account details: ${e.getMessage}")
        Redirect(routes.GuaranteeAccountController.showAccountUnavailable)
      }
  }

  def showAccountUnavailable: Action[AnyContent] = (identify andThen checkEmailIsVerified).async { implicit req =>
    Future.successful(Ok(guaranteeAccountNotAvailable()))
  }
}
