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

import play.api.test.Helpers.running
import utils.SpecBase

class AppConfigSpec extends SpecBase {

  "AppConfig" should {
    "include the app name" in {
      running(application) {
        appConfig.appName mustBe "customs-guarantee-account-frontend"
      }
    }
    
    "contain the correct GOV survay banner URL" in {
      appConfig.helpMakeGovUkBetterUrl mustBe
        "https://survey.take-part-in-research.service.gov.uk/jfe/form/SV_74GjifgnGv6GsMC?Source=BannerList_HMRC_CDS_MIDVA"
    }
  }
}
