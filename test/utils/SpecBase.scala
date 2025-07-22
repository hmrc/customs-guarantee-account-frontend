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
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, BodyParsers}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import repositories.{CacheRepository, RequestedTransactionsCache}
import play.api.Application
import config.AppConfig
import org.mockito.Mockito.reset
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import utils.Utils.{emptyString, singleSpace}
import utils.TestData.sessionId

import scala.reflect.ClassTag

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
    with IntegrationPatience
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = reset(mockRequestedTransactionsCache)

  val mockCacheRepository: CacheRepository                       = mock[CacheRepository]
  val mockRequestedTransactionsCache: RequestedTransactionsCache = mock[RequestedTransactionsCache]

  def applicationBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder()
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

  def fakeRequestWithRequestHeader: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  implicit lazy val messages: Messages = instanceOf[MessagesApi].preferred(fakeRequest(emptyString, singleSpace))

  lazy val application: Application = applicationBuilder.build()

  lazy val appConfig: AppConfig = instanceOf[AppConfig]

  lazy implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(sessionId))

  lazy val bodyParsers: BodyParsers.Default = applicationBuilder.injector().instanceOf[BodyParsers.Default]

  lazy val mockHttpClient: HttpClientV2 = mock[HttpClientV2]

  lazy val requestBuilder: RequestBuilder = mock[RequestBuilder]

  def instanceOf[T: ClassTag]: T = application.injector.instanceOf[T]
}
