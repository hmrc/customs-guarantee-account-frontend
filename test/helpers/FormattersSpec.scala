/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package helpers

import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import services.DateTimeService
import utils.SpecBase

import java.time.{LocalDate, LocalDateTime}


class FormattersSpec extends SpecBase {

  val app = application.build()
  implicit val messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val mockDateTimeService = mock[DateTimeService]
  when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-19T09:30:59"))

  "filenameWithDate" should {
    "return a correctly formatted filename" in {
      running(app){
        val result = Formatters.filenameWithDateTime()(messages, mockDateTimeService)
        result must be("Open_Guarantees_20200419093059.CSV")
      }
      }
  }

  "filenameWithRequestDates" should {
    "return a correctly formatted filename for the given dates" in {
      val fromDate = LocalDate.parse("2020-11-06")
      val toDate = LocalDate.parse("2020-12-12")
      running(app){
        val result = Formatters.filenameWithRequestDates(fromDate, toDate)(messages)
        result must be("Open_Guarantees_202011-202012.CSV")
      }
    }
  }

}