/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package config

import com.google.inject.AbstractModule
import controllers.actions._
import repositories.{CacheRepository, DefaultCacheRepository}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[IdentifierAction]).to(classOf[AuthenticatedIdentifierAction]).asEagerSingleton()
    bind(classOf[CacheRepository]).to(classOf[DefaultCacheRepository]).asEagerSingleton()
    bind(classOf[AuthConnector]).to(classOf[DefaultAuthConnector])
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
  }
}
