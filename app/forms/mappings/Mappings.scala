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

package forms.mappings

import play.api.data.FieldMapping
import play.api.data.Forms.of

import java.time.LocalDate

trait Mappings extends Formatters with Constraints {
  // scalastyle:off
  protected def localDate(
    emptyStartMonth: String,
    emptyStartYear: String,
    emptyEndMonth: String,
    emptyEndYear: String,
    emptyStartDate: String,
    emptyEndDate: String,
    invalidMonth: String,
    invalidYear: String,
    invalidDate: String,
    args: Seq[String] = Seq.empty
  ): FieldMapping[LocalDate] =
    of(
      new LocalDateFormatter(
        emptyStartMonth,
        emptyStartYear,
        emptyEndMonth,
        emptyEndYear,
        emptyStartDate,
        emptyEndDate,
        invalidMonth,
        invalidYear,
        invalidDate,
        args
      )
    )
  // scalastyle:on
}
