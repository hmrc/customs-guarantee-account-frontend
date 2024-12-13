/*
 * Copyright 2024 HM Revenue & Customs
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

package views.components

import config.AppConfig
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.Application
import play.api.i18n.Messages
import utils.SpecBase
import viewmodels._
import views.html.components.pager

import java.time.{LocalDate, Month}

class PagerSpec extends SpecBase {

  "Pager view" should {

    "render correctly with paginated data" when {
      "data fits in one single page" in new Setup {
        val viewDoc: Document = view(paginatedModel)

        shouldNotContainLinkToPreviousPage(viewDoc)
        shouldNotContainPaginationLabel(viewDoc)
      }

      "data does not fit in a single page" in new Setup {
        val viewDoc: Document = view(paginatedModelWithMoreThanOneItem)

        shouldContainPaginationLabel(viewDoc)
      }

      "page contains links to individual pages" in new Setup {
        val viewDoc: Document = view(paginatedModelWithMoreThanOneItem)

        shouldContainContainLinksToPages(viewDoc)
      }
    }
  }

  trait Setup {
    val app: Application     = application.build()
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

    implicit val msg: Messages = messages(app)

    val year2024      = 2024
    val year2018      = 2018
    val monthOfYear   = 1
    val dayOfTheMonth = 22

    val totAmt   = "20.00"
    val clearAmt = "30.00"
    val openAmt  = "10.00"

    val amt: Amounts      = Amounts(totAmt, Some(clearAmt), Some(openAmt), "2020-08-01")
    val tt: TaxType       = TaxType("VAT", amt)
    val ttg: TaxTypeGroup = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt)

    val dd: DueDate =
      DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg))

    val guaranTrans: GuaranteeTransaction =
      GuaranteeTransaction(
        LocalDate.of(year2018, Month.JULY, dayOfTheMonth),
        "MRN-2",
        None,
        BigDecimal(12368.50),
        None,
        "GB30000",
        "GB40000",
        BigDecimal(33.00),
        BigDecimal(27.20),
        None,
        Some("C18-1"),
        dueDates = Seq(dd)
      )

    val guaranteeAccountTrans: GuaranteeAccountTransaction =
      GuaranteeAccountTransaction(guaranteeTransaction = guaranTrans, c18References = Seq(guaranTrans))

    val guaranteeAccTransDate: GuaranteeAccountTransactionsByDate =
      GuaranteeAccountTransactionsByDate(
        LocalDate.of(year2024, monthOfYear, dayOfTheMonth),
        Seq(guaranteeAccountTrans)
      )

    val paginatedModel: Paginated = new Paginated {
      override val itemsGroupedByDate: Seq[GuaranteeAccountTransactionsByDate] = Seq(guaranteeAccTransDate)
      override val itemsPerPage: Int                                           = 10
      override val requestedPage: Int                                          = 1
      override val urlForPage: Int => String                                   = pageNumber => s"/page/$pageNumber"
    }

    val paginatedModelWithMoreThanOneItem: Paginated = new Paginated {
      override val itemsGroupedByDate: Seq[GuaranteeAccountTransactionsByDate] =
        Seq(guaranteeAccTransDate, guaranteeAccTransDate)

      override val itemsPerPage: Int         = 1
      override val requestedPage: Int        = 1
      override val urlForPage: Int => String = pageNumber => s"/page/$pageNumber"
    }

    def view(model: Paginated): Document = Jsoup.parse(app.injector.instanceOf[pager].apply(model).body)
  }

  private def shouldNotContainLinkToPreviousPage(view: Document)(implicit msg: Messages): Assertion = {
    view.getElementsByClass("govuk-pagination__prev").html().contains(msg("cf.pager.prev")) mustBe false
    view
      .getElementsByClass("govuk-pagination__prev")
      .html()
      .contains(msg("cf.pager.summary.accessibility")) mustBe false
  }

  private def shouldContainPaginationLabel(view: Document): Assertion = {
    Option(view.getElementById("pagination-label")) must not be empty
    view.text().contains("Pagination navigation") mustBe true
  }

  private def shouldContainContainLinksToPages(view: Document)(implicit msg: Messages): Assertion = {
    view.getElementsByClass("govuk-pagination__next").html().contains(msg("cf.pager.next")) mustBe true
    view.getElementsByClass("govuk-link").attr("class") must include("govuk-pagination__link")
  }

  private def shouldNotContainPaginationLabel(view: Document): Assertion =
    Option(view.getElementById("pagination-label")) mustBe empty
}
