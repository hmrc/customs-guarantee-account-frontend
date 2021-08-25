/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import models.domain.EORI
import play.api.libs.json.{JsValue, Json, Writes}

import java.time.LocalDate

case class AuditModel(auditType: String, transactionName: String, detail: JsValue)

case class GuaranteeCsvAuditData(eori: EORI, gan: String, openOrClosed: String, asOfDateTime: String, fileFormat: String, from: Option[LocalDate], to: Option[LocalDate])

object GuaranteeCsvAuditData {
  implicit val guaranteeCsvAuditDataWrites: Writes[GuaranteeCsvAuditData] = Json.writes[GuaranteeCsvAuditData]
}
