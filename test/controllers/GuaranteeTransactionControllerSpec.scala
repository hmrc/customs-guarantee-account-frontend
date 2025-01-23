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

package controllers

import connectors.{
  AccountStatusOpen, CustomsFinancialsApiConnector, NoTransactionsAvailable, TooManyTransactionsRequested,
  UnknownException
}
import models.*
import play.api.http.Status
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{DataStoreService, DateTimeService}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.SpecBase
import utils.TestData.{YEAR_2019, balance, dayTwenty, dayTwentyOne, dayTwentyThree, dayTwentyTwo, eori, limit, someGan}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application

import java.time.{LocalDate, LocalDateTime, Month}
import scala.concurrent.Future
import scala.util.Random

class GuaranteeTransactionControllerSpec extends SpecBase {

  "displayTransaction" should {

    "return OK" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Right(ganTransactions)))

      when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-08T12:30:59"))

      when(mockDataStoreService.getEmail(eqTo(eori))(any))
        .thenReturn(Future.successful(Right(Email("abc@test.com"))))

      val application: Application = applicationBuilder
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[DateTimeService].toInstance(mockDateTimeService),
          bind[DataStoreService].toInstance(mockDataStoreService)
        )
        .configure("application.guarantee-account.numberOfItemsPerPage" -> "10")
        .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.GuaranteeTransactionController.displayTransaction("19GB000056HG5w746", None).url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "return Page Not Found if the account does not exist" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(None))

      val application: Application = applicationBuilder
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.GuaranteeTransactionController.displayTransaction("19GB000056HG5w746", None).url)

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
      }
    }

    "display too may results page if more than 5000 securities are returned" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Left(TooManyTransactionsRequested)))

      when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-08T12:30:59"))

      when(mockDataStoreService.getEmail(eqTo(eori))(any))
        .thenReturn(Future.successful(Right(Email("abc@test.com"))))

      val application: Application = applicationBuilder
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[DateTimeService].toInstance(mockDateTimeService),
          bind[DataStoreService].toInstance(mockDataStoreService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.GuaranteeTransactionController.displayTransaction("19GB000056HG5w746", None).url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include regex "There are too many open securities to display consecutively"
      }
    }

    "display show transactions unavailable for runtime exception" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Left(UnknownException)))

      when(mockDataStoreService.getEmail(eqTo(eori))(any))
        .thenReturn(Future.successful(Right(Email("abc@test.com"))))

      val application: Application = applicationBuilder
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[DataStoreService].toInstance(mockDataStoreService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.GuaranteeTransactionController.displayTransaction("19GB000056HG5w746", None).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "display no transactions avaialable for NoTransactionsAvailable" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Left(NoTransactionsAvailable)))

      when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-08T12:30:59"))

      when(mockDataStoreService.getEmail(eqTo(eori))(any))
        .thenReturn(Future.successful(Right(Email("abc@test.com"))))

      val application: Application = applicationBuilder
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[DateTimeService].toInstance(mockDateTimeService),
          bind[DataStoreService].toInstance(mockDataStoreService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.GuaranteeTransactionController.displayTransaction("19GB000056HG5w746", None).url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "display empty transactions when there is no MRN match the decrypted value" in new Setup {
      when(mockCustomsFinancialsApiConnector.getGuaranteeAccount(eqTo(eori))(any, any))
        .thenReturn(Future.successful(Some(guaranteeAccount)))

      when(mockCustomsFinancialsApiConnector.retrieveOpenGuaranteeTransactionsDetail(eqTo(someGan))(any))
        .thenReturn(Future.successful(Right(ganTransactions)))

      when(mockDateTimeService.localDateTime()).thenReturn(LocalDateTime.parse("2020-04-08T12:30:59"))

      when(mockDataStoreService.getEmail(eqTo(eori))(any))
        .thenReturn(Future.successful(Right(Email("abc@test.com"))))

      val application: Application = applicationBuilder
        .overrides(
          bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          bind[DateTimeService].toInstance(mockDateTimeService),
          bind[DataStoreService].toInstance(mockDataStoreService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.GuaranteeTransactionController.displayTransaction("19GB000056HG5w745", None).url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }
  }

  trait Setup {
    val mockDateTimeService: DateTimeService                             = mock[DateTimeService]
    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockDataStoreService: DataStoreService                           = mock[DataStoreService]

    val guaranteeAccount: GuaranteeAccount = GuaranteeAccount(
      someGan,
      eori,
      AccountStatusOpen,
      Some(GeneralGuaranteeBalance(BigDecimal(limit), BigDecimal(balance)))
    )

    val amt: Amounts      = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
    val tt: TaxType       = TaxType("VAT", amt)
    val ttg: TaxTypeGroup = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt)

    val dd: DueDate =
      DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg))

    val ganTransactions: Seq[GuaranteeTransaction] = List(
      GuaranteeTransaction(
        LocalDate.of(YEAR_2019, Month.JULY, dayTwentyThree),
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
        dueDates = Seq(dd)
      ),
      GuaranteeTransaction(
        LocalDate.of(YEAR_2019, Month.JULY, dayTwentyTwo),
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
        dueDates = Seq(dd)
      ),
      GuaranteeTransaction(
        LocalDate.of(YEAR_2019, Month.JULY, dayTwentyOne),
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
        dueDates = Seq(dd)
      ),
      GuaranteeTransaction(
        LocalDate.of(YEAR_2019, Month.JULY, dayTwenty),
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
        dueDates = Seq(dd)
      )
    )

    val nonFatalResponse: UpstreamErrorResponse =
      UpstreamErrorResponse("ServiceUnavailable", Status.SERVICE_UNAVAILABLE, Status.SERVICE_UNAVAILABLE)
  }

  val randomValLimit = 36
  val randomString1  = 18
  val randomString2  = 20
  val randomString3  = 17

  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  def randomFloat: Float = Random.nextFloat()

  def randomLong: Long = Random.nextLong()

  def randomBigDecimal: BigDecimal = BigDecimal(randomFloat.toString)

  def randomLocalDate: LocalDate = LocalDate.now().minusMonths(Random.nextInt(randomValLimit))

  def randomGuaranteeTransaction: GuaranteeTransaction =
    GuaranteeTransaction(
      randomLocalDate,
      randomString(randomString1),
      None,
      randomBigDecimal,
      Some(randomString(randomString2)),
      randomString(randomString3),
      randomString(randomString3),
      randomBigDecimal,
      randomBigDecimal,
      None,
      None,
      Seq.empty
    )

  def randomGuaranteeTransactions(howMany: Int): Seq[GuaranteeTransaction] =
    List.fill(howMany)(randomGuaranteeTransaction)
}
