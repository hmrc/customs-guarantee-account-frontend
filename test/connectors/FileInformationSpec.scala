/*
 * Copyright 2021 HM Revenue & Customs
 *
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

      val expectedFileInformation = FileInformation(
        "filename.txt",
        "https://some.download.domain?token=abc123",
        1234,
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
      val fileInformation = FileInformation(
        "file2.txt",
        "https://some.other.domain?token=abc123",
        9876,
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


