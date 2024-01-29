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

package connectors

import models._
import models.request.IdentifierRequest
import play.api.http.Status
import play.api.inject.bind
import play.api.libs.json.{JsString, JsSuccess}
import play.api.test.Helpers._
import services.MetricsReporterService
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}
import utils.SpecBase

import java.time.{LocalDate, Month}
import scala.concurrent.Future

class CustomsFinancialsApiConnectorSpec extends SpecBase {

  "getAccounts" must {

    "return all accounts available to the given EORI from the API service" in new Setup {
      when[Future[AccountsAndBalancesResponseContainer]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(traderAccounts))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.getGuaranteeAccount(eori)(implicitly,
          IdentifierRequest(fakeRequest(), "12345678")))
        result.value mustEqual guaranteeAccount
      }
    }

    "return unverified email" in new Setup {
      when[Future[EmailUnverifiedResponse]](mockHttpClient.GET(any, any, any)(any, any, any))
        .thenReturn(Future.successful(EmailUnverifiedResponse(Some("unverified@email.com"))))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.isEmailUnverified(hc))
        result mustBe Some("unverified@email.com")
      }
    }

    "log response time metric" in new Setup {
      when[Future[AccountsAndBalancesResponseContainer]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(traderAccounts))

      val mockMetricsReporterService = mock[MetricsReporterService]
      when[Future[Seq[GuaranteeAccount]]](mockMetricsReporterService.withResponseTimeLogging(any)(any)(any))
        .thenReturn(Future.successful(Seq(guaranteeAccount)))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient),
          bind[MetricsReporterService].toInstance(mockMetricsReporterService)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.getGuaranteeAccount(eori)(implicitly,
          IdentifierRequest(fakeRequest(), "12345678")))
        result.value mustEqual guaranteeAccount
        verify(mockMetricsReporterService).withResponseTimeLogging(eqTo(
          "customs-financials-api.get.accounts"))(any)(any)
      }
    }
  }

  "GeneralGuaranteeAccount" must {

    "return correct balance based on guarantee limit and available guarantee balance" in new Setup {
      val ggAcc01 = GeneralGuaranteeAccount(Account("987654", "123456789", "12345678"), Some("999"), Some("900"))
      ggAcc01.toDomain().balances mustBe Some(GeneralGuaranteeBalance(BigDecimal("999"), BigDecimal("900")))

      val ggAcc02 = GeneralGuaranteeAccount(Account("987654", "123456789", "12345678"), None, Some("900"))
      ggAcc02.toDomain().balances mustBe Some(GeneralGuaranteeBalance(BigDecimal("0"), BigDecimal("900")))
    }

  }

  "CDSAccountStatus" must {
    "read correctly based on json value" in new Setup {
      val accStatusVal01 = CDSAccountStatus.CDSAccountStatusReads.reads(JsString("Open"))
      accStatusVal01 mustBe JsSuccess(AccountStatusOpen)

      val accStatusVal02 = CDSAccountStatus.CDSAccountStatusReads.reads(JsString("Suspended"))
      accStatusVal02 mustBe JsSuccess(AccountStatusSuspended)

      val accStatusVal03 = CDSAccountStatus.CDSAccountStatusReads.reads(JsString("Closed"))
      accStatusVal03 mustBe JsSuccess(AccountStatusClosed)

      val accStatusVal04 = CDSAccountStatus.CDSAccountStatusReads.reads(JsString("Unknown"))
      accStatusVal04 mustBe JsSuccess(AccountStatusOpen)
    }

    "write correctly to json value" in new Setup {
      val accStatusVal01 = CDSAccountStatus.CDSAccountStatusWrites.writes(AccountStatusOpen)
      accStatusVal01 mustBe JsString("Open")

      val accStatusVal02 = CDSAccountStatus.CDSAccountStatusWrites.writes(AccountStatusSuspended)
      accStatusVal02 mustBe JsString("Closed")

      val accStatusVal03 = CDSAccountStatus.CDSAccountStatusWrites.writes(AccountStatusClosed)
      accStatusVal03 mustBe JsString("Suspended")
    }
  }

  "retrieveOpenGuaranteeTransactionsDetail" must {
    "return all the transactions from the API when there is no data in the cache" in new Setup {
      when[Future[Seq[GuaranteeTransaction]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(ganTransactions))

      when(mockCacheRepository.get(any)).thenReturn(Future.successful(None))
      when(mockCacheRepository.set(any, any)).thenReturn(Future.successful(true))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveOpenGuaranteeTransactionsDetail("gan")(implicitly))
        result.isRight mustBe true
      }
    }

    "return all the transactions from the API even when setting data in cache failed" in new Setup {
      when[Future[Seq[GuaranteeTransaction]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(ganTransactions))

      when(mockCacheRepository.get(any)).thenReturn(Future.successful(None))
      when(mockCacheRepository.set(any, any)).thenReturn(Future.successful(false))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveOpenGuaranteeTransactionsDetail("gan")(implicitly))
        result.isRight mustBe true
      }
    }

    "return all the transactions from the Cache when there is data present for the GAN" in new Setup {
      when(mockCacheRepository.get(any)).thenReturn(Future.successful(Some(ganTransactions)))


      val app = application.build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveOpenGuaranteeTransactionsDetail("gan")(implicitly))
        result mustBe Right(ganTransactions)
      }
    }

    "return TooManyTransactionsRequested exception if result from the API exceeds maximum limit" in new Setup {

      val upstream4xxResponse = UpstreamErrorResponse("Entity too large to download",
        Status.REQUEST_ENTITY_TOO_LARGE, Status.REQUEST_ENTITY_TOO_LARGE)

      when(mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(upstream4xxResponse))

      when(mockCacheRepository.get(any)).thenReturn(Future.successful(None))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveOpenGuaranteeTransactionsDetail("gan")(implicitly))
        result mustBe Left(TooManyTransactionsRequested)
      }
    }

    "return NotFound exception if no results are returned from the API" in new Setup {
      val upstream4xxResponse = UpstreamErrorResponse("Not Found", Status.NOT_FOUND, Status.NOT_FOUND)

      when(mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(upstream4xxResponse))

      when(mockCacheRepository.get(any)).thenReturn(Future.successful(None))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveOpenGuaranteeTransactionsDetail("gan")(implicitly))
        result mustBe Left(NoTransactionsAvailable)
      }
    }

    "return Unknown exception if no results are returned from the API" in new Setup {

      when(mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new RuntimeException("Boom")))

      when(mockCacheRepository.get(any)).thenReturn(Future.successful(None))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveOpenGuaranteeTransactionsDetail("gan")(implicitly))
        result mustBe Left(UnknownException)
      }
    }
  }


  "retrieveRequestedGuaranteeTransactionsDetail" must {
    "return all the transactions from the API for the requested dates" in new Setup {

      when[Future[Seq[GuaranteeTransaction]]](mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.successful(ganTransactions))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveRequestedGuaranteeTransactionsDetail("gan", true, fromDate, toDate)(implicitly))
        result mustBe Right(ganTransactions)
      }
    }

    "return TooManyTransactionsRequested exception if result from the API exceeds maximum limit" in new Setup {

      val upstream4xxResponse = UpstreamErrorResponse("Entity too large to download",
        Status.REQUEST_ENTITY_TOO_LARGE, Status.REQUEST_ENTITY_TOO_LARGE)

      when(mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(upstream4xxResponse))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient))
        .build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveRequestedGuaranteeTransactionsDetail(
          "gan", true, fromDate, toDate)(implicitly))

        result mustBe Left(TooManyTransactionsRequested)
      }
    }

    "return NotFound exception if no results are returned from the API" in new Setup {

      val upstream4xxResponse = UpstreamErrorResponse("Not Found", Status.NOT_FOUND, Status.NOT_FOUND)
      when(mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(upstream4xxResponse))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveRequestedGuaranteeTransactionsDetail(
          "gan", true, fromDate, toDate)(implicitly))

        result mustBe Left(NoTransactionsAvailable)
      }
    }

    "return Unknown exception if no results are returned from the API" in new Setup {

      when(mockHttpClient.POST(any, any, any)(any, any, any, any))
        .thenReturn(Future.failed(new RuntimeException("Boom")))

      val app = application
        .overrides(
          bind[HttpClient].toInstance(mockHttpClient)
        ).build()

      val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

      running(app) {
        val result = await(connector.retrieveRequestedGuaranteeTransactionsDetail(
          "gan", true, fromDate, toDate)(implicitly))

        result mustBe Left(UnknownException)
      }
    }

  }

  trait Setup {
    private val traderEori = "12345678"
    private val accountNumber = "987654"
    val sessionId = SessionId("session_1234")
    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(sessionId))
    val mockHttpClient = mock[HttpClient]

    val amt = Amounts("20.00", Some("30.00"), Some("10.00"), "2020-08-01")
    val tt = TaxType("VAT", amt)
    val ttg = TaxTypeGroup(taxTypeGroup = "VAT", amounts = amt, taxType = tt)
    val dd = DueDate(dueDate = "2020-07-28", reasonForSecurity = Some("T24"), amounts = amt, taxTypeGroups = Seq(ttg))

    val ganTransactions = List(
      GuaranteeTransaction(LocalDate.of(2019, Month.OCTOBER, 23),
        "19GB000056HG5w746",
        None,
        BigDecimal(45367.12),
        Some("MGH-500000"),
        "GB10000",
        "GB20000",
        BigDecimal(21.00),
        BigDecimal(11.50),
        None,
        None,
        dueDates = Seq(dd)),

      GuaranteeTransaction(LocalDate.of(2019, Month.OCTOBER, 22),
        "18GB011056HG5w747",
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

      GuaranteeTransaction(LocalDate.of(2019, Month.OCTOBER, 22),
        "18GB011056HG5w747",
        None,
        BigDecimal(12368.50),
        None,
        "GB30000",
        "GB40000",
        BigDecimal(33.00),
        BigDecimal(27.20),
        None,
        Some("C18-1747"),
        dueDates = Seq(dd)),

      GuaranteeTransaction(LocalDate.of(2019, Month.OCTOBER, 22),
        "18GB011056HG5w747",
        None,
        BigDecimal(12369.50),
        None,
        "GB30000",
        "GB40000",
        BigDecimal(32.00),
        BigDecimal(26.20),
        None,
        Some("C18-1809"),
        dueDates = Seq(dd))
    )

    val generalGuaranteeAccount = GeneralGuaranteeAccount(Account(
      accountNumber, "123456789", traderEori), Some("999.99"), None)

    val guaranteeAccount = generalGuaranteeAccount.toDomain()

    val fromDate = LocalDate.parse("2019-10-08")
    val toDate = LocalDate.parse("2020-04-08")

    val eori = "123456789"
    val traderAccounts = AccountsAndBalancesResponseContainer(
      AccountsAndBalancesResponse(
        Some(AccountResponseCommon("", Some(""), "", None)),
        AccountResponseDetail(
          Some("123456789"),
          None,
          Some(Seq(generalGuaranteeAccount)))
      )
    )
  }
}
