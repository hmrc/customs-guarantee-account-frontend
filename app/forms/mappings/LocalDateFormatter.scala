/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package forms.mappings

import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
                                            invalidKey: String,
                                            endOfMonth: Boolean,
                                            args: Seq[String]
                                          ) extends Formatter[LocalDate] with Formatters {
  private val fieldKeys: List[String] = List("month", "year")

  private def toDate( key: String, month: Int, year: Int ): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, 1)) match {
      case Success(date) =>
        Right(LocalDate.of(year, month, if (endOfMonth) date.lengthOfMonth() else 1))
      case Failure(_) =>
        Left(Seq(FormError(key, invalidKey, args)))
    }

  private def formatDate( key: String, data: Map[String, String] ): Either[Seq[FormError], LocalDate] = {

    val int = intFormatter(
      requiredKey = invalidKey,
      wholeNumberKey = invalidKey,
      nonNumericKey = invalidKey,
      args
    )

    for {
      month <- int.bind(s"$key.month", data).right
      year <- int.bind(s"$key.year", data).right
      date <- toDate(key, month, year).right
    } yield date
  }

  override def bind( key: String, data: Map[String, String] ): Either[Seq[FormError], LocalDate] = {

    val fields = fieldKeys.map {
      field =>
        field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    fields.count(_._2.isDefined) match {
      case 2 =>
        formatDate(key, data).left.map {
          _.map(_.copy(key = key, args = args))
        }
      case _ =>
        Left(List(FormError(key, invalidKey, args)))
    }
  }

  override def unbind( key: String, value: LocalDate ): Map[String, String] =
    Map(
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year" -> value.getYear.toString
    )
}


