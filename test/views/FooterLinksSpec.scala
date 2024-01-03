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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.footer.FooterItem
import utils.SpecBase

class FooterLinksSpec extends SpecBase {

  "apply" should {

    "return correct FooterItems" in new Setup {
      val footerLinks: Seq[FooterItem] = FooterLinks()(msges, appConfig)

      footerLinks.size mustEqual (4)
    }
  }

  trait Setup {
    val app = application.build()
    val appConfig = app.injector.instanceOf[AppConfig]
    implicit val msges: Messages = messages(app)
  }
}