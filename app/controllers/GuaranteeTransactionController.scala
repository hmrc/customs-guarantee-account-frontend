/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import config.{AppConfig, ErrorHandler}
import connectors.{CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested, UnknownException}
import controllers.actions.IdentifierAction
import crypto.UrlEncryption
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.{GuaranteeAccountTransactionsViewModel, GuaranteeAccountViewModel}
import views.html.{guarantee_account, guarantee_account_exceed_threshold, individual_transaction}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeTransactionController @Inject()(identify: IdentifierAction,
                                               apiConnector: CustomsFinancialsApiConnector,
                                               tooManyResults: guarantee_account_exceed_threshold,
                                               guaranteeAccount: guarantee_account,
                                               dateTimeService: DateTimeService,
                                               view: individual_transaction,
                                               errorHandler: ErrorHandler,
                                               urlEncryption: UrlEncryption,
                                               mcc: MessagesControllerComponents)(implicit execution: ExecutionContext, val appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  def displayTransaction(mrn: String, page: Option[Int]): Action[AnyContent] = identify async { implicit request =>
    val decryptedMrn = urlEncryption.decrypt(mrn)
    apiConnector.getGuaranteeAccount(request.eori).flatMap {
      case None => Future.successful(NotFound(errorHandler.notFoundTemplate))
      case Some(account) => apiConnector.retrieveOpenGuaranteeTransactionsDetail(account.number).map {
        case Left(value) => value match {
          case NoTransactionsAvailable => Ok(guaranteeAccount(GuaranteeAccountViewModel(account, dateTimeService.localDateTime()), GuaranteeAccountTransactionsViewModel(Seq.empty, page)))
          case TooManyTransactionsRequested => Ok(tooManyResults(GuaranteeAccountViewModel(account, dateTimeService.localDateTime())))
          case UnknownException => Redirect(routes.GuaranteeAccountController.showTransactionsUnavailable())
        }
        case Right(transactions) =>
          transactions.find(_.movementReferenceNumber == decryptedMrn) match {
            case Some(value) => Ok(view(GuaranteeAccountViewModel(account, dateTimeService.localDateTime()), value, page))
            case None => Ok(guaranteeAccount(GuaranteeAccountViewModel(account, dateTimeService.localDateTime()), GuaranteeAccountTransactionsViewModel(Seq.empty, page)))
          }
      }
    }
  }
}
