/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models.request

import models.domain.EORI
import play.api.libs.json.{Json, OWrites}

case class AccountAuthoritiesRequest(eori: EORI)

object AccountAuthoritiesRequest {
  implicit val AccountAuthoritiesRequestWrites: OWrites[AccountAuthoritiesRequest] = Json.writes[AccountAuthoritiesRequest]
}