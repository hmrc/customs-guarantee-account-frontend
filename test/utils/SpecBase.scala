/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package utils

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import controllers.actions.{FakeIdentifierAction, IdentifierAction}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import repositories.CacheRepository

class FakeMetrics extends Metrics {
  override val defaultRegistry: MetricRegistry = new MetricRegistry
  override val toJson: String = "{}"
}

trait SpecBase extends AnyWordSpecLike with Matchers with MockitoSugar with OptionValues with ScalaFutures with IntegrationPatience {

  val mockCacheRepository: CacheRepository = mock[CacheRepository]

  def application: GuiceApplicationBuilder = new GuiceApplicationBuilder().overrides(
      bind[IdentifierAction].to[FakeIdentifierAction],
      bind[Metrics].toInstance(new FakeMetrics),
      bind[CacheRepository].toInstance(mockCacheRepository),
    ).configure("auditing.enabled" -> false)
    .configure("metrics.enabled" -> false)

  def fakeRequest(method: String = "", path: String = ""): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, path).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
}
