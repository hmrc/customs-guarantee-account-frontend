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

import play.api.libs.json.Json
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
}
