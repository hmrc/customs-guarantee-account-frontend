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

package config

import play.api.Application
import play.api.i18n.MessagesApi
import utils.SpecBase
import views.html.{ErrorTemplate, not_found}
import play.api.test.Helpers.*
import play.twirl.api.Html

import scala.concurrent.ExecutionContext.Implicits.global

class ErrorHandlerSpec extends SpecBase {

  "ErrorHandler" must {

    "return an error page" in new Setup {
      val result: Html = await(
        errorHandler.standardErrorTemplate(
          pageTitle = "pageTitle",
          heading = "heading",
          message = "message"
        )(fakeRequestWithRequestHeader)
      )

      result.body must include("pageTitle")
      result.body must include("heading")
      result.body must include("message")
    }

    "return notFound template" in new Setup {
      val result: Html = await(errorHandler.notFoundTemplate(fakeRequestWithRequestHeader))

      result.body must include(messages("cf.error.not-found.heading"))
      result.body must include(messages("cf.error.not-found.message.address-typed-wrong"))
      result.body must include(messages("cf.error.not-found.message.address-pasted-wrong"))
    }
  }

  trait Setup {
    val app: Application = applicationBuilder.build()

    implicit val config: AppConfig           = appConfig
    private val messageApi: MessagesApi      = app.injector.instanceOf[MessagesApi]
    private val errorTemplate: ErrorTemplate = app.injector.instanceOf[ErrorTemplate]
    private val notFoundTemplate: not_found  = app.injector.instanceOf[not_found]

    val errorHandler: ErrorHandler = new ErrorHandler(errorTemplate, notFoundTemplate, messageApi)
  }
}
