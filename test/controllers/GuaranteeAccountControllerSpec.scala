/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import connectors.{AccountStatusOpen, CustomsFinancialsApiConnector, TooManyTransactionsRequested}
import models._
import org.jsoup.Jsoup.parseBodyFragment
import play.api.http.Status
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AuditingService, DateTimeService}
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase

import java.time.{LocalDate, LocalDateTime, Month}
import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.Random

class GuaranteeAccountControllerSpec extends SpecBase {

  "show account details" should {

    "return OK" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Right(ganTransactions)))

      when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-08T12:30:59"))

      val app = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[DateTimeService].toInstance(mockDateTimeService)
        )
        .configure(
          "features.guarantee-account-details" -> "true",
          "application.guarantee-account.numberOfItemsPerPage" -> "10")
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.GuaranteeAccountController.showAccountDetails(None).url)
        val result = route(app, request).value
        status(result) mustEqual OK
      }
    }

    "return Page Not Found if the account does not exist" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(None))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.GuaranteeAccountController.showAccountDetails(None).url)
        val result = route(app, request).value
        status(result) mustEqual NOT_FOUND
      }
    }
    "include a link to the guarantee transactions request page when feature switch is set to true" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Right(ganTransactions)))

      when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-08T12:30:59"))
      val app = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[DateTimeService].toInstance(mockDateTimeService)
        )
        .configure(
          "application.guarantee-account.numberOfItemsPerPage" -> "10")
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.GuaranteeAccountController.showAccountDetails(None).url)
        val result = route(app, request).value
        contentAsString(result) must include regex "Search for and download a CSV of all securities"
      }

    }
    "display too may results page if more than 5000 securities are returned" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-08T12:30:59"))

      val app = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[DateTimeService].toInstance(mockDateTimeService)
        )
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.GuaranteeAccountController.showAccountDetails(None).url)
        val result = route(app, request).value
        status(result) mustEqual OK
        contentAsString(result) must include regex "There are too many open securities to display consecutively"
      }

    }

  }
  "showTransactionsUnavailable" should {
    "return OK" in new Setup {

      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-08T12:30:59"))

      val app = application
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[DateTimeService].toInstance(mockDateTimeService)
        )
        .configure(
          "application.guarantee-account.numberOfItemsPerPage" -> "10")
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.GuaranteeAccountController.showTransactionsUnavailable.url)
        val result = route(app, request).value
        status(result) mustEqual OK
      }

    }

    "redirect to account unavailable page" when {
      "failed to fetch account details while redirecting to transaction unavailable page" in new Setup {
        val upstream5xxResponse = UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)
        when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
          .thenReturn(Future.failed(upstream5xxResponse))

        val app = application
          .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          val request = FakeRequest(GET, routes.GuaranteeAccountController.showTransactionsUnavailable.url)
          val result = route(app, request).value
          status(result) must be(Status.SEE_OTHER)
          header(LOCATION, result) must be(Some(routes.GuaranteeAccountController.showAccountUnavailable.url))
        }
      }
      "the api returns Not Found" in new Setup {
        val errorResponse = UpstreamErrorResponse("Not Found", Status.NOT_FOUND, Status.NOT_FOUND)
        when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
          .thenReturn(Future.failed(errorResponse))

        val app = application
          .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          val request = FakeRequest(GET, routes.GuaranteeAccountController.showTransactionsUnavailable.url)
          val result = route(app, request).value
          status(result) must be(Status.SEE_OTHER)
          header(LOCATION, result) must be(Some(routes.GuaranteeAccountController.showAccountUnavailable.url))
        }
      }

      "the api returns entity too large" in new Setup {
        val errorResponse = UpstreamErrorResponse("Entity too large", Status.REQUEST_ENTITY_TOO_LARGE, Status.REQUEST_ENTITY_TOO_LARGE)
        when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
          .thenReturn(Future.failed(errorResponse))

        val app = application
          .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          val request = FakeRequest(GET, routes.GuaranteeAccountController.showTransactionsUnavailable.url)
          val result = route(app, request).value
          status(result) must be(Status.SEE_OTHER)
          header(LOCATION, result) must be(Some(routes.GuaranteeAccountController.showAccountUnavailable.url))
        }
      }
    }
  }

  "showAccountUnavailable" should {
    "return OK" in new Setup {

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.GuaranteeAccountController.showAccountUnavailable.url)
        val result = route(app, request).value
        status(result) must be(Status.OK)
      }
    }
  }

  "unhappy path section" should {

    "redirect to an error page" when {
      "the api connector throws an exception" in new Setup {

        when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
          .thenReturn(Future.failed(new RuntimeException("boom")))

        val app = application
          .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
          .build()

        running(app) {
          val request = FakeRequest(GET, routes.GuaranteeAccountController.showAccountDetails(None).url)
          val result = route(app, request).value
          status(result) must be(Status.SEE_OTHER)
          header(LOCATION, result) must be(Some(routes.GuaranteeAccountController.showAccountUnavailable.url))
        }
      }
    }
  }

  "paginator" should {
    "be compatible with the page sort and order query parameter" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Right(randomGuaranteeTransactions(555))))

      val app = application
        .overrides(bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector))
        .configure(
          "application.guarantee-account.numberOfItemsPerPage" -> "10")
        .build()

      running(app) {
        val request = FakeRequest(GET, routes.GuaranteeAccountController.showAccountDetails(None).url + "?sortBy=date&order=ascending&page=5")
        val result = route(app, request).value
        val html = parseBodyFragment(contentAsString(result)).body
        val pageNumberLinks = html.select("li.govuk-pagination__number > a").asScala
        withClue("html did not contain any pagination links:") {
          pageNumberLinks.size must not be (0)
        }
        pageNumberLinks.map { pageNumberLink =>
          withClue(s"page number link $pageNumberLink must include page number") {
            pageNumberLink.attr("href") must include(s"page=" + pageNumberLink.text())
          }
        }
      }
    }
  }

  trait Setup {

    val eori = "GB001"
    val someGan = "GAN-1"

    val mockDateTimeService = mock[DateTimeService]
    val mockCustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockAuditingService: AuditingService = mock[AuditingService]

    val guaranteeAccount = GuaranteeAccount(someGan, eori, AccountStatusOpen, Some(GeneralGuaranteeBalance(
      BigDecimal(123000),
      BigDecimal(123.45)
    )))

    val amt = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
    val tt = TaxType("VAT", amt)
    val ttg = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt)
    val dd = DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg))

    val ganTransactions = List(
      GuaranteeTransaction(LocalDate.of(2018, Month.JULY, 23),
        "MRN-1",
        None,
        BigDecimal(45367.12),
        Some("UCR-1"),
        "GB10000",
        "GB20000",
        BigDecimal(21.00),
        BigDecimal(11.50),
        None,
        None,
        dueDates = Seq(dd)),

      GuaranteeTransaction(LocalDate.of(2018, Month.JULY, 22),
        "MRN-2",
        None,
        BigDecimal(12367.50),
        None,
        "GB30000",
        "GB40000",
        BigDecimal(31.00),
        BigDecimal(25.20),
        None,
        None,
        dueDates = Seq(dd)),

      GuaranteeTransaction(LocalDate.of(2018, Month.JULY, 21),
        "MRN-3",
        None,
        BigDecimal(12368.50),
        None,
        "GB30000",
        "GB40000",
        BigDecimal(33.00),
        BigDecimal(27.20),
        None,
        Some("C18-1"),
        dueDates = Seq(dd)),

      GuaranteeTransaction(LocalDate.of(2018, Month.JULY, 20),
        "MRN-4",
        None,
        BigDecimal(12369.50),
        None,
        "GB30000",
        "GB40000",
        BigDecimal(32.00),
        BigDecimal(26.20),
        None,
        Some("C18-2"),
        dueDates = Seq(dd))
    )
    val nonFatalResponse = UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)
  }

  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  def randomFloat: Float = Random.nextFloat()

  def randomLong: Long = Random.nextLong()

  def randomBigDecimal: BigDecimal = BigDecimal(randomFloat.toString)

  def randomLocalDate: LocalDate = LocalDate.now().minusMonths(Random.nextInt(36))

  def randomGuaranteeTransaction: GuaranteeTransaction =
    GuaranteeTransaction(
      randomLocalDate,
      randomString(18),
      None,
      randomBigDecimal,
      Some(randomString(20)),
      randomString(17),
      randomString(17),
      randomBigDecimal,
      randomBigDecimal,
      None,
      None,
      Seq.empty
    )

  def randomGuaranteeTransactions(howMany: Int): Seq[GuaranteeTransaction] = List.fill(howMany)(randomGuaranteeTransaction)
}
