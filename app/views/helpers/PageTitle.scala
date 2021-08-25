/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package views.helpers

import play.api.i18n.Messages

object PageTitle {

  def fullPageTitle(title: Option[String])(implicit messages: Messages): Option[String] = {
     title match {
       case Some(text) => Some(s"$text - ${messages("service.name")} - GOV.UK")
       case _ => Some(s"${messages("service.name")} - GOV.UK")
      }
  }
}
