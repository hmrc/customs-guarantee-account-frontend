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

package viewmodels

import config.AppConfig
import controllers.routes
import models.GuaranteeAccountTransaction

import java.time.LocalDate

case class GuaranteeAccountTransactionsByDate(date: LocalDate, transactions: Seq[GuaranteeAccountTransaction])

case class GuaranteeAccountTransactionsViewModel(transactions: Seq[GuaranteeAccountTransaction],
                                                 pageNumber: Option[Int])
                                                (implicit val appConfig: AppConfig) extends Paginated {

  val downloadUrl: String = routes.DownloadCsvController.downloadCsv(page = pageNumber, disposition = None).url
  val allItems: Seq[GuaranteeAccountTransaction] = transactions

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)
  val itemsGroupedByDate: Seq[GuaranteeAccountTransactionsByDate] = transactions.groupBy(
    _.guaranteeTransaction.date).map(v => GuaranteeAccountTransactionsByDate(
      v._1, v._2)).toSeq.sortBy(_.date).reverse

  val itemsPerPage: Int = appConfig.numberOfItemsPerPage
  val requestedPage: Int = pageNumber.getOrElse(1)

  override val urlForPage: Int => String = e =>
    routes.GuaranteeAccountController.showAccountDetails(Some(e)).url
}

