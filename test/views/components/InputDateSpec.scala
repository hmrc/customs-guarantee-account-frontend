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

package views.components

import forms.GuaranteeTransactionsRequestPageFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.SpecBase
import java.time.Clock
import utils.Utils.emptyString

class InputDateSpec extends SpecBase {
  "InpuDate component" should {

    "render correctly with no errors" in new Setup {
      val formWithValues = form.bind(Map(
        "start.month" -> "01", "start.year" -> "2021", "end.month" -> "10", "end.year" -> "2021"))

      running(app) {
        val inputDateView = app.injector.instanceOf[views.html.components.inputDate]
        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues, "Date of Birth", id = "start", legendHiddenContent = None)(messages(app))
        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("h1").text() must include("Date of Birth")
        html.getElementById("start.month").attr("value") must include("01")
        html.getElementById("start.year").attr("value") must include("2021")

        html.getElementsByClass("govuk-date-input__input").attr(
          "class") mustNot include("govuk-input--error")
      }
    }

    "render correctly with month error" in new Setup {

      val formWithValues = form.bind(Map(
        "start.month" -> emptyString, "start.year" -> "2021",
        "end.month" -> "10", "end.year" -> "2021"))

      running(app) {
        val inputDateView = app.injector.instanceOf[views.html.components.inputDate]
        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues, "Date of Birth", id = "start", legendHiddenContent = None)(messages(app))
        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("h1").text() must include("Date of Birth")
        html.getElementById("start.month").attr("value") mustNot include("01")
        html.getElementById("start.year").attr("value") must include("2021")

        html.getElementsByClass("govuk-date-input__input").attr(
          "class") must include("govuk-input--error")
      }
    }

    "render correctly with year error" in new Setup {

      val formWithValues = form.bind(Map(
        "start.month" -> "01", "start.year" -> emptyString, "end.month" -> "10", "end.year" -> "2021"))

      running(app) {
        val inputDateView = app.injector.instanceOf[views.html.components.inputDate]
        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues, "Date of Birth", id = "start", legendHiddenContent = None)(messages(app))
        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("h1").text() must include("Date of Birth")
        html.getElementById("start.month").attr("value") must include("01")
        html.getElementById("start.year").attr("value") mustNot include("2021")

        html.getElementsByClass("govuk-date-input__input").attr(
          "class") mustNot include("govuk-input--error")
      }
    }

    "render correctly with both month and year errors" in new Setup {

      val formWithValues = form.bind(Map(
        "start.month" -> emptyString, "start.year" -> emptyString,
        "end.month" -> "10", "end.year" -> "2021"))

      running(app) {
        val inputDateView = app.injector.instanceOf[views.html.components.inputDate]

        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues, "Date of Birth", id = "start", legendHiddenContent = None)(messages(app))

        val html: Document = Jsoup.parse(output.toString)

        html.getElementsByTag("h1").text() must include("Date of Birth")
        html.getElementById("start.month").attr("value") mustNot include("01")
        html.getElementById("start.year").attr("value") mustNot include("2021")

        html.getElementsByClass("govuk-date-input__input").attr(
          "class") must include("govuk-input--error")
      }
    }
  }

  trait Setup {
    implicit val clk: Clock = Clock.systemUTC()
    val form = new GuaranteeTransactionsRequestPageFormProvider().apply()
    val app: Application = application.build()
  }
}
