/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers.actions

import com.google.inject.Inject
import config.AppConfig
import controllers.routes
import models.request.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: AppConfig,
                                               val parser: BodyParsers.Default
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions {
  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(Retrievals.credentials and Retrievals.name and Retrievals.email
      and Retrievals.affinityGroup and Retrievals.internalId and Retrievals.allEnrolments) {
      case Some(_) ~ _ ~ _ ~ Some(_) ~ Some(_) ~ allEnrolments =>
        allEnrolments.getEnrolment("HMRC-CUS-ORG").flatMap(_.getIdentifier("EORINumber")) match {
          case Some(eori) => block(IdentifierRequest(request, eori.value))
          case None => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
        }
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue_url" -> Seq(config.loginContinueUrl)))
      case _: InsufficientEnrolments =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }
}
