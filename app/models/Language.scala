/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.i18n.Lang
import play.api.mvc.PathBindable

sealed trait Language {
  val lang: Lang
}

object Language {

  case object Cymraeg extends WithName("cymraeg") with Language {
    override val lang: Lang = Lang("cy")
  }

  case object English extends WithName("english") with Language {
    override val lang: Lang = Lang("en")
  }

  implicit def pathBindable: PathBindable[Language] = new PathBindable[Language] {
    override def bind(key: String, value: String): Either[String, Language] =
      value match {
        case Cymraeg.toString => Right(Cymraeg)
        case English.toString => Right(English)
        case _                => Left("Invalid language")
      }

    override def unbind(key: String, value: Language): String =
      value.toString
  }
}
