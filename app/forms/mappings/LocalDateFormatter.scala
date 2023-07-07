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

import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
                                            invalidMonth: String,
                                            invalidYear: String,
                                            emptyStartMonth: String,
                                            emptyStartYear: String,
                                            emptyEndMonth: String,
                                            emptyEndYear: String,
                                            emptyStartDate: String,
                                            emptyEndDate: String,
                                            endOfMonth: Boolean,
                                            args: Seq[String]
                                          ) extends Formatter[LocalDate] with Formatters {
  private val fieldKeys: List[String] = List("month", "year")

  private def toDate( key: String, month: Int, year: Int ): Either[Seq[FormError], LocalDate] =
    validMonth(month) match {
      case true =>
        Try(LocalDate.of(year, month, 1)) match {
          case Success(date) =>
            Right(LocalDate.of(year, month, if (endOfMonth) date.lengthOfMonth() else 1))
          case Failure(_) =>
            Left(Seq(FormError(updateFormErrorKeys(key, month, year), invalidYear, args)))
        }
      case _ => Left(Seq(FormError(updateFormErrorKeys(key, month, year), invalidMonth, args)))
    }


  private def formatDate( key: String, data: Map[String, String] ): Either[Seq[FormError], LocalDate] = {

    val intMonth = intFormatter(
      requiredKey = invalidMonth,
      wholeNumberKey = invalidMonth,
      nonNumericKey = invalidMonth,
      args
    )

    val intYear = intFormatter(
      requiredKey = invalidYear,
      wholeNumberKey = invalidYear,
      nonNumericKey = invalidYear,
      args
    )

    for {
      month <- intMonth.bind(s"$key.month", data)
      year <- intYear.bind(s"$key.year", data)
      date <- toDate(key, month, year)
    } yield date
  }

  override def bind( key: String, data: Map[String, String] ): Either[Seq[FormError], LocalDate] = {

    val fields = fieldKeys.map {
      field =>
        field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 2 =>
        formatDate(key, data).left.map {
          _.map(_.copy(key = key, args = args))
        }
      case 1 =>
        (key, missingFields.head) match {
          case ("start", "month") => Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), emptyStartMonth, args)))
          case ("start", "year") => Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), emptyStartYear, args)))
          case ("end", "month") => Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), emptyEndMonth, args)))
          case ("end", "year") => Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), emptyEndYear, args)))
        }
      case _ =>
        (key, missingFields) match {
          case ("start", List("month", "year")) => Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), emptyStartDate, args)))
          case ("end", List("month","year")) => Left(List(FormError(formErrorKeysInCaseOfEmptyOrNonNumericValues(key, data), emptyEndDate, args)))
        }
    }
  }

  private def validMonth(month: Int): Boolean = {
    month > 0 && month < 13
    }


  override def unbind( key: String, value: LocalDate ): Map[String, String] =
    Map(
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year" -> value.getYear.toString
    )

    /**
   * Updates the FormError key as per valid month and year criteria
   * key is updated as below
   * invalid month - key.month
   * invalid year - key.year
   *
   * @param key Input form key
   * @param month Month value of the input date
   * @param year Year value of the input date
   * @return Updated form key
   */
  private[mappings] def updateFormErrorKeys(key: String,
                                            month: Int,
                                            year: Int): String =
    (month, year) match {
      case (m, _) if m < 1 || m > 12 => s"$key.month"
      case (_, y) if y < 1000 || y > 99999 => s"$key.year"
      case _ => s"$key.month"
    }

  /**
   * Updates the FormError key as per the given criteria
   * key is updated as below
   * empty month or non numeric month - key.month
   * empty year or non numeric year - key.year
   *
   * @param key FormError key
   * @param data Map of Form values
   * @return Updated FormError key
   */
  private[mappings] def formErrorKeysInCaseOfEmptyOrNonNumericValues(key: String,
                                                                     data: Map[String, String]): String = {

    val monthValue = data.get(s"$key.month")
    val yearValue = data.get(s"$key.year")

    (monthValue, yearValue) match {
      case (Some(m), _) if m.trim.isEmpty || Try(m.trim.toInt).isFailure => s"$key.month"
      case (_, Some(y)) if y.trim.isEmpty || Try(y.trim.toInt).isFailure => s"$key.year"
      case _ => s"$key.month"
    }
  }
}
