/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.request

import models.RequestDates
import models.domain.GAN
import play.api.libs.json.{Json, OWrites}



case class GuaranteeTransactionsRequest(gan: GAN, openItems: Boolean, dates: Option[RequestDates])

object GuaranteeTransactionsRequest {
  implicit val OpenGuaranteeTransactionsRequestWrites: OWrites[GuaranteeTransactionsRequest] = Json.writes[GuaranteeTransactionsRequest]
}