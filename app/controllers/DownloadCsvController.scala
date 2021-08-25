/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import cats.data.EitherT.{fromOptionF, liftF}
import cats.instances.future._
import config.{AppConfig, ErrorHandler}
import connectors._
import controllers.actions.IdentifierAction
import helpers.FileFormatters
import models.{GuaranteeAccount, GuaranteeTransaction, RequestDates}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{AuditingService, DateTimeService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.CSVWriter.Seq2CSV
import viewmodels.GuaranteeTransactionCsvRow.GuaranteeTransactionCsvRowViewModel
import viewmodels.ResultsPageSummary
import views.html.{guarantee_account_unable_download_csv, guarantee_transactions_no_result, guarantee_transactions_too_many_results}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class DownloadCsvController @Inject()(
                                       identify: IdentifierAction,
                                       apiConnector: CustomsFinancialsApiConnector,
                                       tooManyResults: guarantee_transactions_too_many_results,
                                       noResults: guarantee_transactions_no_result,
                                       auditingService: AuditingService,
                                       unableToDownloadCSV: guarantee_account_unable_download_csv)
                                     (implicit ec: ExecutionContext,
                                      dateTimeService: DateTimeService,
                                      mcc: MessagesControllerComponents,
                                      eh: ErrorHandler,
                                      appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport with FileFormatters {

  val log: Logger = Logger(this.getClass)

  def downloadCsv(disposition: Option[String],
                  page: Option[Int]): Action[AnyContent] = identify.async { implicit request =>
    val eventualMaybeGuaranteeAccount = apiConnector.getGuaranteeAccount(request.eori)
    val result = for {
      account <- fromOptionF[Future, Result, GuaranteeAccount](eventualMaybeGuaranteeAccount, NotFound(eh.notFoundTemplate))
      transactions <- liftF[Future, Result, Either[GuaranteeResponses, Seq[GuaranteeTransaction]]](apiConnector.retrieveOpenGuaranteeTransactionsDetail(account.number))
      result = transactions match {
        case Left(_) =>  Redirect(routes.DownloadCsvController.showUnableToDownloadCSV(page))
        case Right(transactions) =>
          val csvContent = convertToCSV(transactions)
          val contentHeaders = "Content-Disposition" -> s"${disposition.getOrElse("attachment")}; filename=$filenameWithDateTime"
          val _ = auditingService.auditCsvDownload(request.eori, account.number, dateTimeService.utcDateTime(), None)
          Ok(csvContent).withHeaders(contentHeaders)
      }
    } yield result

    result.merge.recover {
      case NonFatal(t) =>
        log.error(s"Unable to download CSV: ${t.getMessage}")
        InternalServerError
    }
  }

  def downloadRequestedCsv(disposition: Option[String],
                           from: String, to: String,
                           page: Option[Int]): Action[AnyContent] = identify.async { implicit request =>
    Try(LocalDate.parse(from), LocalDate.parse(to)) match {
      case Failure(_) => Future.successful(BadRequest)
      case Success((start, end)) =>
        val eventualMaybeGuaranteeAccount = apiConnector.getGuaranteeAccount(request.eori)
        val result = for {
          account <- fromOptionF[Future, Result, GuaranteeAccount](eventualMaybeGuaranteeAccount, NotFound(eh.notFoundTemplate))
          transactions <- liftF[Future, Result, Either[GuaranteeResponses, Seq[GuaranteeTransaction]]](apiConnector.retrieveRequestedGuaranteeTransactionsDetail(account.number, onlyOpenItems = true, start, end))
          result = transactions match {
            case Left(error) => error match {
              case NoTransactionsAvailable => Ok(noResults(new ResultsPageSummary(start, end)))
              case TooManyTransactionsRequested => Ok(tooManyResults(new ResultsPageSummary(start, end), controllers.routes.GuaranteeTransactionsRequestPageController.onPageLoad().url))
              case UnknownException => Redirect(routes.DownloadCsvController.showUnableToDownloadCSV(page))
            }
            case Right(transactions) =>
              val csvContent = convertToCSV(transactions)
              val contentHeaders = "Content-Disposition" -> s"${disposition.getOrElse("attachment")}; filename=${filenameWithRequestDates(start, end)}"
              val _ = auditingService.auditCsvDownload(request.eori, account.number, dateTimeService.utcDateTime(), Some(RequestDates(start, end)))
              Ok(csvContent).withHeaders(contentHeaders)
          }
        } yield result

        result.merge.recover {
          case NonFatal(t) =>
            log.error(s"Unable to download requested CSV: ${t.getMessage}")
            InternalServerError
        }
    }
  }

  def showUnableToDownloadCSV(page: Option[Int]): Action[AnyContent] = identify { implicit req =>
    Ok(unableToDownloadCSV(page))
  }

  private def convertToCSV(transactions: Seq[GuaranteeTransaction])(implicit messages: Messages) = {
    val fileFooter = Some(messages("cf.guarantee-account.csv.guidance", appConfig.guaranteeAccountGuidanceUrl))
    transactions.flatMap(_.toReportLayout).toCSVWithHeaders(makeColumnNames, fileFooter)
  }

  private def makeColumnNames(columnName: String)(implicit messages: Messages): String = {
    val messagePrefix = "cf.guarantee-account.csv"
    val messageKey = s"$messagePrefix.$columnName"
    messages(messageKey)
  }
}
