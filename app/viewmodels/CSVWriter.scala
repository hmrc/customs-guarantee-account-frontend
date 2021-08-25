/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package viewmodels

object CSVWriter {

  private def quote(string: String): String = s""""$string""""

  implicit class Product2CSV(val caseClass: Product) extends AnyVal {

    def toCSV: String = caseClass.productIterator.map{
      case nestedProduct: Product => nestedProduct.toCSV
      case string: String => quote(string)
      case rest => rest
    }.mkString(",")

  }

  implicit class Seq2CSV(val sequence: Seq[Product]) extends AnyVal {

    def toCSVWithHeaders(mappingFn: String => String = identity, footer: Option[String] = None): String = {
      val mapAndQuote: String => String = mappingFn andThen quote
      val headers: String = sequence.headOption.map(fieldNames(_).map(mapAndQuote).mkString(",") + "\n").getOrElse("")
      val formattedFooter: String = footer.fold("")(text => s"""\n\n\n"$text"\n""")
      headers + sequence.toCSV + formattedFooter
    }

    def toCSV: String = sequence.map(_.toCSV).mkString("\n")

    private def fieldNames(caseClass: Product): Array[String] = {
      caseClass.getClass.getDeclaredFields.map(_.getName).filterNot(_ == "$outer")
    }

  }

}

