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

package connectors

import play.api.libs.json.{JsResultException, JsSuccess, Json}
import utils.SpecBase

class FileInformationSpec extends SpecBase {

  "FileInformation" should {

    "be able to read from json" in {

      val json =
        """  {
          |    "filename": "filename.txt",
          |    "downloadURL": "https://some.download.domain?token=abc123",
          |    "fileSize": 1234,
          |    "metadata": [
          |      { "metadata": "item1", "value": "value1" },
          |      { "metadata": "item2", "value": "value2" }
          |    ]
          |  }""".stripMargin

      val expectedFileInformationVals = 1234

      val expectedFileInformation = FileInformation(
        "filename.txt",
        "https://some.download.domain?token=abc123",
        expectedFileInformationVals,
        Metadata(
          List(
            MetadataItem("item1", "value1"),
            MetadataItem("item2", "value2")
          )
        )
      )

      import connectors.FileInformation._
      val fileInformation = Json.parse(json).as[FileInformation]
      fileInformation must be(expectedFileInformation)

    }

    "be able to write to json" in {
      val fileInfoVals = 9876

      val fileInformation = FileInformation(
        "file2.txt",
        "https://some.other.domain?token=abc123",
        fileInfoVals,
        Metadata(
          List(
            MetadataItem("item3", "value3"),
            MetadataItem("item4", "value4")
          )
        )
      )

      val expectedJson =
        """  {
          |    "filename": "file2.txt",
          |    "downloadURL": "https://some.other.domain?token=abc123",
          |    "fileSize": 9876,
          |    "metadata": [
          |      { "metadata": "item3", "value": "value3" },
          |      { "metadata": "item4", "value": "value4" }
          |    ]
          |  }""".stripMargin

      import connectors.FileInformation._
      val json = Json.toJson(fileInformation)
      json must be(Json.parse(expectedJson))
    }
  }

  "metadataItemReads" should {
    "Read the object correctly" in new Setup {
      import FileInformation.metadataItemReads

      Json.fromJson(Json.parse(metaDataItemInputJsString)) mustBe JsSuccess(metaDataItemObject)
    }

    "throw exception for invalid Json" in {
      import FileInformation.metadataItemReads
      val invalidJson = "{ \"key1\": \"test_key\", \"value1\": \"test_event\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[MetadataItem]
      }
    }
  }

  "metadataItemWrites" should {
    "Write the object correctly" in new Setup {
      import FileInformation.metadataItemWrites

      Json.toJson(metaDataItemObject) mustBe Json.parse(metaDataItemJsString)
    }
  }

  "metadataReads" should {
    "Read the object correctly" in new Setup {
      import FileInformation.metadataReads

      Json.fromJson(Json.parse(metaDataJsString)) mustBe JsSuccess(metaDataObject)
    }

    "throw exception for invalid Json" in {
      import FileInformation.metadataReads
      val invalidJson = "{ \"item\": \"test_key\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[Metadata]
      }
    }
  }

  "metadataWrites" should {
    "Write the object correctly" in new Setup {
      import FileInformation.metadataWrites

      Json.toJson(metaDataObject) mustBe Json.parse(metaDataJsString)
    }
  }

  trait Setup {
    val metaDataItemObject: MetadataItem = MetadataItem("test_key", "test_key_value")

    val metaDataItemJsString: String      = """{"key":"test_key","value":"test_key_value"}""".stripMargin
    val metaDataItemInputJsString: String = """{"metadata":"test_key","value":"test_key_value"}""".stripMargin

    val metaDataObject: Metadata = Metadata(Seq(MetadataItem("test_key", "test_value")))
    val metaDataJsString: String = """[{"metadata":"test_key","value":"test_value"}]""".stripMargin
  }
}
