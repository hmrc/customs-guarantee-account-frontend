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
import utils.SpecBase

import scala.concurrent.Future
import org.mongodb.scala._
import org.mongodb.scala.result.DeleteResult
import play.api.test.Helpers.running
import play.api.{Application, inject}
import uk.gov.hmrc.mongo.MongoComponent
import utils.Utils.emptyString

class DbPatchServiceSpec extends SpecBase {

  "Database Patch Service" should {

    "delete documents when deleteGuaranteeAccountCacheDocuments is true" in new Setup {
      when(appConfig.deleteGuaranteeAccountCacheDocuments).thenReturn(true)
      commonSetupForDeletion()

      running(app) {
        new DbPatchService(appConfig, mockComponent)

        whenReady(mockDeleteObservable.toFuture()) { result =>
          result.getDeletedCount mustBe documents
        }
      }
    }

    "not delete documents when deleteGuaranteeAccountCacheDocuments is false" in new Setup {
      when(appConfig.deleteGuaranteeAccountCacheDocuments).thenReturn(false)
      when(mockComponent.database).thenReturn(mockDatabase)

      running(app) {
        new DbPatchService(appConfig, mockComponent)

        verify(mockComponent.database, never).getCollection(collectionName)
      }
    }

    "delete all documents and ensure collection is empty" in new Setup {
      when(appConfig.deleteGuaranteeAccountCacheDocuments).thenReturn(true)
      when(mockCollection.countDocuments()).thenReturn(SingleObservable(emptyCollection))
      commonSetupForDeletion()

      running(app) {
        new DbPatchService(appConfig, mockComponent)

        whenReady(mockDeleteObservable.toFuture()) { result =>
          result.getDeletedCount mustBe documents

          whenReady(mockCollection.countDocuments().toFuture()) { count =>
            count mustBe emptyCollection
          }
        }
      }
    }

    "handle exception during document deletion" in new Setup {
      when(appConfig.deleteGuaranteeAccountCacheDocuments).thenReturn(true)
      commonSetupForException()

      running(app) {
        new DbPatchService(appConfig, mockComponent)

        verify(mockDatabase).getCollection[Document](eqTo(collectionName))(any, any)
        verify(mockCollection).deleteMany(any)
      }
    }
  }

  trait Setup {
    val collectionName = "guarantee-account-cache"
    val documents = 5
    val timesCalled = 1
    val emptyCollection = 0

    val appConfig: AppConfig = mock[AppConfig]
    val mockComponent: MongoComponent = mock[MongoComponent]
    val mockDatabase: MongoDatabase = mock[MongoDatabase]
    val mockCollection: MongoCollection[Document] = mock[MongoCollection[Document]]
    val mockDeleteResult: DeleteResult = mock[DeleteResult]
    val mockDeleteObservable: SingleObservable[DeleteResult] = mock[SingleObservable[DeleteResult]]

    when(appConfig.customsFinancialsApi).thenReturn(emptyString)

    val app: Application = application.overrides(
      inject.bind[MongoComponent].toInstance(mockComponent),
      inject.bind[AppConfig].toInstance(appConfig)).build()

    def commonSetupForDeletion(): Unit = {
      when(mockComponent.database).thenReturn(mockDatabase)
      when(mockDatabase.getCollection[Document](eqTo(collectionName))(any, any)).thenReturn(mockCollection)
      when(mockCollection.deleteMany(any)).thenReturn(mockDeleteObservable)
      when(mockDeleteObservable.toFuture()).thenReturn(Future.successful(mockDeleteResult))
      when(mockDeleteResult.getDeletedCount).thenReturn(documents)
    }

    def commonSetupForException(): Unit = {
      when(mockComponent.database).thenReturn(mockDatabase)
      when(mockDatabase.getCollection[Document](eqTo(collectionName))(any, any)).thenReturn(mockCollection)
      when(mockCollection.deleteMany(any)).thenReturn(mockDeleteObservable)
      when(mockDeleteObservable.toFuture()).thenReturn(Future.failed(new Exception("Deletion error")))
    }
  }
}
