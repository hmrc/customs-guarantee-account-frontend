/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package forms.mappings

import play.api.data.FieldMapping
import play.api.data.Forms.of

import java.time.LocalDate

trait Mappings extends Formatters with Constraints {

  protected def localDate(
                           invalidKey: String,
                           endOfMonth: Boolean,
                           args: Seq[String]): FieldMapping[LocalDate] =
    of(new LocalDateFormatter(invalidKey, endOfMonth, args))
}
