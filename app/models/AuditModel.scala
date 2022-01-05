/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import models.domain.EORI
import play.api.libs.json.{JsValue, Json, Writes}

import java.time.LocalDate

case class AuditModel(auditType: String, transactionName: String, detail: JsValue)

case class GuaranteeCsvAuditData(eori: EORI, gan: String, openOrClosed: String, asOfDateTime: String, fileFormat: String, from: Option[LocalDate], to: Option[LocalDate])

object GuaranteeCsvAuditData {
  implicit val guaranteeCsvAuditDataWrites: Writes[GuaranteeCsvAuditData] = Json.writes[GuaranteeCsvAuditData]
}
