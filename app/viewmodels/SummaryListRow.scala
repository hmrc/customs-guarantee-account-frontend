/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package viewmodels

import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

final case class SummaryListRow(
                                 value: Value,
                                 secondValue: Option[Value],
                                 classes: String,
                                 actions: Option[Actions]
                               )
