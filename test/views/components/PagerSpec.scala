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

import play.api.Application
import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.SpecBase
import viewmodels._
import views.html.components.pager


class PagerSpec extends SpecBase {

  class MockPaginated extends Paginated {
    val itemsGroupedByDate: Seq[GuaranteeAccountTransactionsByDate] = Seq.empty
    val itemsPerPage: Int = 10
    val requestedPage: Int = 1
    val urlForPage: Int => String = pageNumber => s"/page/$pageNumber"
  }

  "Pagination view" should {

    "render correctly with paginated data" in new Setup {

      val paginatedModel = new MockPaginated

      running(app) {
        val output: HtmlFormat.Appendable = view(paginatedModel)(messages(app))
        val html: Document = Jsoup.parse(output.toString)

        println(s"================ ${html}")

      }
    }
  }


  trait Setup {
    val app: Application = application.build()
    val appConfig = app.injector.instanceOf[AppConfig]
  
    val view: pager = app.injector.instanceOf[pager]

  }
}