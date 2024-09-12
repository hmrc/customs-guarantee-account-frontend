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

package services

import config.AppConfig
import org.mongodb.scala.{Document, MongoCollection}
import play.api.Logger
import uk.gov.hmrc.mongo.MongoComponent

import javax.inject.{Inject, Singleton}

@Singleton
class DbPatchService @Inject()(appConfig: AppConfig, mongoComponent: MongoComponent) {

  val log = Logger(this.getClass)

  if (appConfig.dropGuaranteeAccCache) {
    log.info("appConfig.dropGuaranteeAccCache is true")
    dropCollection("guarantee-account-cache")
  }
  else {
    log.info("appConfig.dropGuaranteeAccCache is false")
  }

  private def dropCollection(collectionName: String): Unit = {
    log.info(s"Started collection drop : ${collectionName}")
    try {
      val collection: MongoCollection[Document] = mongoComponent.database.getCollection(collectionName)
      collection.drop().toFuture()
      log.info(s"Collection dropped : ${collectionName}")
    } catch {
      case e: Exception => log.error(s"Collection drop failed : ${collectionName} with Exception : ${e.getMessage}")
    }
  }

}
