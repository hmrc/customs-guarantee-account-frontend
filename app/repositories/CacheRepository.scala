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

package repositories

import crypto._
import models._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import org.mongodb.scala.model.Indexes.ascending
import play.api.Configuration
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.time.{LocalDate, LocalDateTime}

@Singleton
class DefaultCacheRepository @Inject()(mongoComponent: MongoComponent,
                                       config: Configuration,
                                       guaranteeTransactionsEncryptor: GuaranteeTransactionsEncryptor,
                                       guaranteeTransactionsDecryptor: GuaranteeTransactionsDecryptor)(
  implicit executionContext: ExecutionContext)
  extends PlayMongoRepository[GuaranteeAccountMongo](
    collectionName = "guarantee-account-cache",
    mongoComponent = mongoComponent,
    domainFormat = GuaranteeAccountMongo.format,
    indexes = Seq(
      IndexModel(
        ascending("lastUpdated"),
        IndexOptions()
          .name("guarantee-account-cache-last-updated-index")
          .expireAfter(config.get[Int]("mongodb.timeToLiveInSeconds"),
            TimeUnit.SECONDS)
    )
  )) with CacheRepository {

  private val encryptionKey = config.get[String]("mongodb.encryptionKey")

  def get(id: String): Future[Option[Seq[GuaranteeTransaction]]] = {
    for {
      result <- collection.find(equal("_id", id)).toSingle().toFutureOption()
      account = result.map(mongoAccount => guaranteeTransactionsDecryptor.decryptGuaranteeTransactions(
        mongoAccount.transactions, encryptionKey))
    } yield account
  }

  def set(id: String, transactions: Seq[GuaranteeTransaction]): Future[Boolean] = {
    val record = GuaranteeAccountMongo(
      guaranteeTransactionsEncryptor.encryptGuaranteeTransactions(transactions, encryptionKey),
      LocalDateTime.now()
    )

    collection.replaceOne(
      equal("_id", id),
      record,
      ReplaceOptions().upsert(true)
    ).toFuture().map(_.wasAcknowledged())
  }
}

case class GuaranteeAccountMongo(transactions: Seq[EncryptedGuaranteeTransaction],
                                 lastUpdated: LocalDateTime = LocalDateTime.now())

  object GuaranteeAccountMongo {
    implicit val javaTimeFormat: Format[LocalDate] = MongoJavatimeFormats.localDateFormat
    implicit val format: OFormat[GuaranteeAccountMongo] = Json.format[GuaranteeAccountMongo]
  }

trait CacheRepository {
  def get(id: String): Future[Option[Seq[GuaranteeTransaction]]]
  def set(id: String, transactions: Seq[GuaranteeTransaction]): Future[Boolean]
}
