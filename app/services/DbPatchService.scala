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
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{Document, MongoCollection}
import play.api.Logger
import uk.gov.hmrc.mongo.MongoComponent

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DbPatchService @Inject()(appConfig: AppConfig, mongoComponent: MongoComponent)(implicit val ec: ExecutionContext) {

  private val log: Logger = Logger(this.getClass)
  private val documentName = "guarantee-account-cache"

  if (appConfig.deleteGuaranteeAccountCacheDocuments) {
    log.info("appConfig.deleteGuaranteeAccountCacheDocuments is true")
    deleteDocumentsFromCache(documentName)
  } else {
    log.info("appConfig.deleteGuaranteeAccountCacheDocuments is false")
  }

  private def deleteDocumentsFromCache(collectionName: String): Unit = {
    log.warn(s"Started deletion of documents : $collectionName")
    val collection = getMongoCollection(collectionName)

    deleteDocuments(collection, collectionName)
  }

  private def getMongoCollection(collectionName: String): MongoCollection[Document] = {
    mongoComponent.database.getCollection(collectionName)
  }

  private def deleteDocuments(collection: MongoCollection[Document], collectionName: String): Unit = {
    collection.deleteMany(empty())
      .toFuture()
      .map { result =>
        log.warn(s"Deleted documents from : $collectionName")
        log.warn(s"Total deleted documents in the $collectionName: ${result.getDeletedCount}")
      }.recover {
        case e: Exception =>
          log.error(s"Collection drop failed : $collectionName with Exception : ${e.getMessage}")
      }
  }
}
