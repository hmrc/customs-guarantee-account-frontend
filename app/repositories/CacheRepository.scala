/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package repositories


import crypto._
import models._
import org.joda.time.DateTime
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import org.mongodb.scala.model.Indexes.ascending
import play.api.Configuration
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DefaultCacheRepository @Inject()(mongoComponent: MongoComponent,
                                       config: Configuration,
                                       guaranteeTransactionsEncryptor: GuaranteeTransactionsEncryptor,
                                       guaranteeTransactionsDecryptor: GuaranteeTransactionsDecryptor)(implicit executionContext: ExecutionContext)
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
      account = result.map(mongoAccount => guaranteeTransactionsDecryptor.decryptGuaranteeTransactions(mongoAccount.transactions, encryptionKey))
    } yield account
  }

  def set(id: String, transactions: Seq[GuaranteeTransaction]): Future[Boolean] = {
    val record = GuaranteeAccountMongo(
      guaranteeTransactionsEncryptor.encryptGuaranteeTransactions(transactions, encryptionKey),
      DateTime.now()
    )

    collection.replaceOne(
      equal("_id", id),
      record,
      ReplaceOptions().upsert(true)
    ).toFuture().map(_.wasAcknowledged())
  }
}

case class GuaranteeAccountMongo(transactions: Seq[EncryptedGuaranteeTransaction], lastUpdated: DateTime = DateTime.now())

  object GuaranteeAccountMongo {
    implicit val jodaTimeFormat: Format[DateTime] = MongoJodaFormats.dateTimeFormat
    implicit val format: OFormat[GuaranteeAccountMongo] = Json.format[GuaranteeAccountMongo]
  }


trait CacheRepository {
  def get(id: String): Future[Option[Seq[GuaranteeTransaction]]]
  def set(id: String, transactions: Seq[GuaranteeTransaction]): Future[Boolean]
}
