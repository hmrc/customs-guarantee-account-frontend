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

import config.AppConfig
import play.api.i18n.Messages
import utils.SpecBase

class PaginatedSpec extends SpecBase {

  "Paginated" should {

    "return valid title when page summary is invoked" in new Setup {

      val testPage: Paginated = new Paginated {
        override val itemsGroupedByDate: Seq[GuaranteeAccountTransactionsByDate] = Seq.empty
        override val itemsPerPage: Int = 10
        override val requestedPage: Int = 1
        override val urlForPage: Int => String = pageNumber =>
          s"https://gov.uk/customs-guarantee-account-frontend/page/${pageNumber}"
      }

      testPage.pageSummary(msgs) mustBe msgs("cf.pager.summary")
    }
  }

  trait Setup {
    val app = application.build()
    implicit val msgs: Messages = messages(app)
    val appConfig = app.injector.instanceOf[AppConfig]
  }
}
