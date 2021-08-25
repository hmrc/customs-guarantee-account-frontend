/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import play.api.libs.functional.syntax._
import play.api.libs.json._


case class FileInformation(filename: String,
                           downloadURL: String,
                           fileSize: Long,
                           metadata: Metadata)

case class MetadataItem(key: String, value: String)

case class Metadata(items: Seq[MetadataItem]) {
  val asMap: Map[String, String] = items.map(item => (item.key, item.value)).toMap
}

object FileInformation {
  implicit val metadataItemReads: Reads[MetadataItem] =
    ((JsPath \ "metadata").read[String] and (JsPath \ "value").read[String]) (MetadataItem.apply _)
  implicit val metadataReads: Reads[Metadata] = __.read[List[MetadataItem]].map(Metadata.apply)
  implicit val metadataItemWrites: Writes[MetadataItem] =  Json.writes[MetadataItem]
  implicit val metadataWrites: Writes[Metadata] = new Writes[Metadata] {
    override def writes(o: Metadata): JsValue = {
      JsArray(o.items.map(
        item => Json.obj(("metadata", item.key), ("value", item.value))))
    }
  }
  implicit val fileInformationFormats: Format[FileInformation] = Json.format[FileInformation]
}
