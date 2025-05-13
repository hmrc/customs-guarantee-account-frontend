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
import cats.data.EitherT.{fromOptionF, liftF}
import cats.instances.future.*
import config.AppConfig
import connectors.*
import controllers.actions.{EmailAction, IdentifierAction}
import helpers.FileFormatters
import models.{GuaranteeAccount, GuaranteeTransaction, RequestDates}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import services.{AuditingService, DateTimeService}
import repositories.RequestedTransactionsCache
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.GuaranteeTransactionCsvRow.GuaranteeTransactionCsvRowViewModel
import viewmodels.{CSVWriter, ResultsPageSummary}
import views.html.{
  guarantee_account_unable_download_csv, guarantee_transactions_no_result, guarantee_transactions_too_many_results,
  not_found
}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class DownloadCsvController @Inject() (
  identify: IdentifierAction,
  checkEmailIsVerified: EmailAction,
  apiConnector: CustomsFinancialsApiConnector,
  tooManyResults: guarantee_transactions_too_many_results,
  noResults: guarantee_transactions_no_result,
  auditingService: AuditingService,
  cache: RequestedTransactionsCache,
  unableToDownloadCSV: guarantee_account_unable_download_csv,
  notFound: not_found
)(implicit
  ec: ExecutionContext,
  dateTimeService: DateTimeService,
  mcc: MessagesControllerComponents,
  appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport
    with FileFormatters {

  val log: Logger = Logger(this.getClass)

  def downloadCsv(disposition: Option[String], page: Option[Int]): Action[AnyContent] =
    (identify andThen checkEmailIsVerified).async { implicit request =>

      val eventualMaybeGuaranteeAccount = apiConnector.getGuaranteeAccount(request.eori)

      val result = for {
        account <- fromOptionF[Future, Result, GuaranteeAccount](eventualMaybeGuaranteeAccount, NotFound(notFound()))

        transactions <- liftF[Future, Result, Either[GuaranteeResponses, Seq[GuaranteeTransaction]]](
                          apiConnector.retrieveOpenGuaranteeTransactionsDetail(account.number)
                        )

        result = transactions match {
                   case Left(_)             => Redirect(routes.DownloadCsvController.showUnableToDownloadCSV(page))
                   case Right(transactions) =>
                     val csvContent = convertToCSV(transactions)

                     val contentHeaders = "Content-Disposition" ->
                       s"${disposition.getOrElse("attachment")}; filename=${filenameWithDateTime()}"

                     val _ = auditingService.auditCsvDownload(
                       request.eori,
                       account.number,
                       dateTimeService.utcDateTime(),
                       None
                     )

                     Ok(csvContent).withHeaders(contentHeaders)
                 }
      } yield result

      result.merge.recover { case NonFatal(t) =>
        log.error(s"Unable to download CSV: ${t.getMessage}")
        InternalServerError
      }
    }

  def downloadRequestedCsv(
    disposition: Option[String],
    from: String,
    to: String,
    page: Option[Int]
  ): Action[AnyContent] = (identify andThen checkEmailIsVerified).async { implicit request =>
    Try {
      (LocalDate.parse(from), LocalDate.parse(to))
    } match {
      case Failure(_)            => Future.successful(BadRequest)
      case Success((start, end)) =>
        val eventualMaybeGuaranteeAccount = apiConnector.getGuaranteeAccount(request.eori)
        val result                        = for {
          account <- fromOptionF[Future, Result, GuaranteeAccount](eventualMaybeGuaranteeAccount, NotFound(notFound()))

          transactions <-
            liftF[Future, Result, Either[GuaranteeResponses, Seq[GuaranteeTransaction]]](
              apiConnector
                .retrieveRequestedGuaranteeTransactionsDetail(account.number, onlyOpenItems = true, start, end)
            )

          result <- EitherT.liftF(
                      Future.successful(
                        processTransactions(transactions, disposition, start, end, page, account.number, request.eori)
                      )
                    )
        } yield result

        result.merge.recover { case NonFatal(t) =>
          log.error(s"Unable to download requested CSV: ${t.getMessage}")
          InternalServerError
        }
    }
  }

  private def processTransactions(
    transactions: Either[GuaranteeResponses, Seq[GuaranteeTransaction]],
    disposition: Option[String],
    start: LocalDate,
    end: LocalDate,
    page: Option[Int],
    accountNumber: String,
    eori: String
  )(implicit request: Request[_]): Result =
    cache.clear(eori)

    transactions match {
      case Left(error) =>
        error match {
          case NoTransactionsAvailable      => Ok(noResults(new ResultsPageSummary(start, end)))
          case TooManyTransactionsRequested =>
            Ok(
              tooManyResults(
                new ResultsPageSummary(start, end),
                controllers.routes.RequestTransactionsController.onPageLoad().url
              )
            )
          case UnknownException             => Redirect(routes.DownloadCsvController.showUnableToDownloadCSV(page))
        }

      case Right(transactions) =>
        val csvContent     = convertToCSV(transactions)
        val contentHeaders = "Content-Disposition" ->
          s"${disposition.getOrElse("attachment")}; filename=${filenameWithRequestDates(start, end)}"

        auditingService.auditCsvDownload(
          eori,
          accountNumber,
          dateTimeService.utcDateTime(),
          Some(RequestDates(start, end))
        )

        Ok(csvContent).withHeaders(contentHeaders)
    }

  def showUnableToDownloadCSV(page: Option[Int]): Action[AnyContent] = (identify andThen checkEmailIsVerified) {
    implicit req =>
      Ok(unableToDownloadCSV(page))
  }

  private def convertToCSV(transactions: Seq[GuaranteeTransaction])(implicit messages: Messages) = {
    val fileFooter = Some(messages("cf.guarantee-account.csv.guidance", appConfig.guaranteeAccountGuidanceUrl))
    CSVWriter.toCSVWithHeaders(transactions.flatMap(_.toReportLayout), makeColumnNames, fileFooter)
  }

  private def makeColumnNames(columnName: String)(implicit messages: Messages): String = {
    val messagePrefix = "cf.guarantee-account.csv"
    val messageKey    = s"$messagePrefix.$columnName"
    messages(messageKey)
  }
}
