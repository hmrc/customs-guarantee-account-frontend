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

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.{
  DefaultMessagesApi,
  Lang,
  MessagesImpl,
  Messages,
  MessagesProvider
}
import play.api.inject.bind
import utils.SpecBase
import views.html.components.inputDate


class InputDateSpec extends SpecBase {

  "InpuDate component" should {
    "render correctly with no errors" in {
      val form =
        new FakeForm().bind(Map("value.month" -> "07", "value.year" -> "2023"))
      val messages = MessagesImpl(Lang.defaultLang, new DefaultMessagesApi())

      val output = inputDate.render(form, "Date of Birth", messages)

      contentAsString(output) must include("Date of Birth")
      contentAsString(output) must include("July")
      contentAsString(output) must include("2023")
      contentAsString(output) mustNot include("govuk-input--error")
    }

    "render correctly with month error" in {
      val form =
        new FakeForm().bind(Map("value.month" -> "", "value.year" -> "2023"))
      val messages = MessagesImpl(Lang.defaultLang, new DefaultMessagesApi())

      val output = inputDate.render(form, "Date of Birth", messages)

      contentAsString(output) must include("Date of Birth")
      contentAsString(output) mustNot include("July")
      contentAsString(output) must include("2023")
      contentAsString(output) must include("govuk-input--error")
    }

    "render correctly with year error" in {
      val form =
        new FakeForm().bind(Map("value.month" -> "07", "value.year" -> ""))
      val messages = MessagesImpl(Lang.defaultLang, new DefaultMessagesApi())

      val output = inputDate.render(form, "Date of Birth", messages)

      contentAsString(output) must include("Date of Birth")
      contentAsString(output) must include("July")
      contentAsString(output) mustNot include("2023")
      contentAsString(output) must include("govuk-input--error")
    }

    "render correctly with both month and year errors" in {
      val form =
        new FakeForm().bind(Map("value.month" -> "", "value.year" -> ""))
      val messages = MessagesImpl(Lang.defaultLang, new DefaultMessagesApi())

      val output = inputDate.render(form, "Date of Birth", messages)

      contentAsString(output) must include("Date of Birth")
      contentAsString(output) mustNot include("July")
      contentAsString(output) mustNot include("2023")
      contentAsString(output) must include("govuk-input--error")
    }
  }

  // Fake implementation of Play's Form class for testing purposes
  class FakeForm {
    def bind(data: Map[String, String]): FakeForm = this
  }
}
