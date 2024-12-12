/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels

import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Value}
import utils.Utils.emptyString

trait SummaryListRowHelper {

  def summaryListRow(value: String, secondValue: Option[String] = None, actions: Actions): SummaryListRow =
    SummaryListRow(
      value = Value(content = HtmlContent(value)),
      secondValue = secondValue.map(value => Value(content = HtmlContent(value))),
      classes = emptyString,
      actions = Some(actions)
    )

  def span(contents: String): HtmlContent = HtmlContent(
    Html(s"""$contents""")
  )
}
