/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package viewmodels

import models.GuaranteeTransactionDates
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions

import java.time.LocalDate

class ResultsPageSummary(from: LocalDate, to: LocalDate)(implicit messages: Messages) extends SummaryListRowHelper {

  def rows:SummaryListRow = {
    guaranteeTransactionsResultRow(GuaranteeTransactionDates(from, to))
  }

  def guaranteeTransactionsResultRow(dates: GuaranteeTransactionDates): SummaryListRow = {
      summaryListRow(
        value = HtmlFormat.escape(
          messages("date.range",
            dateAsMonthAndYear(dates.start),
            dateAsMonthAndYear(dates.end))
        ).toString,
        actions = Actions(items = Seq(ActionItem(
          href = controllers.routes.DownloadCsvController.downloadRequestedCsv(Some("attachment"), dates.start.toString, dates.end.toString, None).url,
          content = span(messages("cf.guarantee-account.detail.csv")),
          visuallyHiddenText = Some(messages("cf.guarantee-account.detail.csv-definition"))
        ))))
  }

  def dateAsMonthAndYear(date: LocalDate)(implicit messages: Messages): String = s"${dateAsMonth(date)} ${date.getYear}"

  def dateAsMonth(date: LocalDate)(implicit messages: Messages): String = messages(s"month.${date.getMonthValue}")
}
