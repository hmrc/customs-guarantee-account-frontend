/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.request

import play.api.mvc.{Request, WrappedRequest}

trait RequestWithEori[A] extends Request[A]

final case class IdentifierRequest[A] (request: Request[A], eori: String)
  extends WrappedRequest[A](request) with RequestWithEori[A]
