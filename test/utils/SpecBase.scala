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

package utils

import com.codahale.metrics.MetricRegistry
import controllers.actions.{FakeIdentifierAction, IdentifierAction}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import repositories.{CacheRepository, RequestedTransactionsCache}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import utils.Utils.{emptyString, singleSpace}

class FakeMetrics extends MetricRegistry {
  val defaultRegistry: MetricRegistry = new MetricRegistry
  val toJson: String                  = "{}"
}

trait SpecBase
    extends AnyWordSpecLike
    with Matchers
    with MockitoSugar
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val mockCacheRepository: CacheRepository                       = mock[CacheRepository]
  val mockRequestedTransactionsCache: RequestedTransactionsCache = mock[RequestedTransactionsCache]

  def application: GuiceApplicationBuilder = new GuiceApplicationBuilder()
    .overrides(
      bind[IdentifierAction].to[FakeIdentifierAction],
      bind[MetricRegistry].toInstance(new FakeMetrics),
      bind[CacheRepository].toInstance(mockCacheRepository),
      bind[RequestedTransactionsCache].toInstance(mockRequestedTransactionsCache)
    )
    .configure("auditing.enabled" -> false)
    .configure("metrics.enabled" -> false)

  def fakeRequest(method: String = emptyString, path: String = emptyString): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, path).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messages(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, singleSpace))
}
