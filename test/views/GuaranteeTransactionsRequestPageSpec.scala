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

package views

import config.AppConfig
import models.GuaranteeTransactionDates
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.SpecBase
import views.html.guarantee_transactions_request_page
import forms.GuaranteeTransactionsRequestPageFormProvider

import java.time.Clock

class GuaranteeTransactionsRequestPageSpec extends SpecBase {

  "GuaranteeTransactionsRequestPage view" should {

    "display correct guidance and text" in new Setup {

      view.title().contains(s"${messages(app)("cf.guarantee-account.transactions.request.title")}")
      view.getElementsByTag("h1").text() mustBe messages(app)("cf.guarantee-account.transactions.request.heading")

      textDoc.contains(messages(app)("cf.guarantee-account.transactions.request.from")) mustBe true
      textDoc.contains(messages(app)("cf.guarantee-account.transactions.request.startDate.hint")) mustBe true
      textDoc.contains(messages(app)("cf.guarantee-account.transactions.request.endDate.hint")) mustBe true
      textDoc.contains(messages(app)("cf.guarantee-account.transactions.request.to")) mustBe true
    }

    "not display previous or incorrect guidance and text" in new Setup {

      textDoc.contains("For example, 3 2019.") mustBe false
      textDoc.contains("You can request guarantee account securities dating back to October 2019") mustBe false
      textDoc.contains("what end date do you need securities?") mustBe false
      textDoc.contains("what start date do you need securities?") mustBe false
    }
  }

  trait Setup {

    val app: Application = application.build()

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")
    implicit val clock: Clock = Clock.systemUTC()

    val form: Form[GuaranteeTransactionDates] = new GuaranteeTransactionsRequestPageFormProvider().apply()

    val view: Document = Jsoup.parse(app.injector.instanceOf[guarantee_transactions_request_page].apply(form).body)
    val textDoc = view.text()
  }
}
