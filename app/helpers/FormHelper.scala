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

package helpers

object FormHelper {

  def updateFormErrorKeyForStartAndEndDate(): (String, String) => String = (key: String, errorMsg: String) => {

    val olderStartDateKey    = "cf.form.error.start.date-too-far-in-past"
    val earlierStartDateKey  = "cf.form.error.startDate.date-earlier-than-system-start-date"
    val futureStartDateKey   = "cf.form.error.start-future-date"
    val invalidStartMonthKey = "cf.form.error.start.month.date-number-invalid"
    val invalidStartYearKey  = "cf.form.error.start.year.date-number-invalid"

    val olderEndDateKey    = "cf.form.error.end.date-too-far-in-past"
    val earlierEndDateKey  = "cf.form.error.endDate.date-earlier-than-system-start-date"
    val futureEndDateKey   = "cf.form.error.end-future-date"
    val invalidEndMonthKey = "cf.form.error.end.month.date-number-invalid"
    val invalidEndYearKey  = "cf.form.error.end.year.date-number-invalid"

    val startDateMsgKeyList =
      List(olderStartDateKey, earlierStartDateKey, futureStartDateKey, invalidStartMonthKey, invalidStartYearKey)

    val endDateMsgKeyList =
      List(olderEndDateKey, earlierEndDateKey, futureEndDateKey, invalidEndMonthKey, invalidEndYearKey)

    if (key.equals("start") || key.equals("end")) {
      if (startDateMsgKeyList.contains(errorMsg) || endDateMsgKeyList.contains(errorMsg)) {
        s"$key.month"
      } else {
        s"$key.year"
      }
    } else {
      key
    }
  }
}
