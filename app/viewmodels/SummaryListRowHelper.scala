/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package viewmodels

import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Value}

trait SummaryListRowHelper {

  def summaryListRow(
                      value: String,
                      secondValue: Option[String] = None,
                      actions: Actions): SummaryListRow =
    SummaryListRow(
      value = Value(content = HtmlContent(value)),
      secondValue = secondValue.map { value => Value(content = HtmlContent(value)) },
      classes = "",
      actions = Some(actions)
    )

  def span(contents: String): HtmlContent = HtmlContent(
    Html(s"""$contents""")
  )
}