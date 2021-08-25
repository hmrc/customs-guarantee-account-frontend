/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package viewmodels

import config.AppConfig
import controllers.routes
import crypto.UrlEncryption
import models.{GuaranteeAccountTransaction, GuaranteeTransaction}

import java.time.LocalDate


case class GuaranteeAccountTransactionsByDate(date: LocalDate, transactions: Seq[GuaranteeAccountTransaction])

case class GuaranteeAccountTransactionsViewModel(transactions: Seq[GuaranteeAccountTransaction],
                                                 pageNumber: Option[Int])
                                                (implicit val appConfig: AppConfig) extends Paginated {

  val downloadUrl: String = routes.DownloadCsvController.downloadCsv(page = pageNumber, disposition = None).url
  val allItems: Seq[GuaranteeAccountTransaction] = transactions

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)
  val itemsGroupedByDate: Seq[GuaranteeAccountTransactionsByDate] = transactions.groupBy(_.guaranteeTransaction.date).map(v => GuaranteeAccountTransactionsByDate(v._1, v._2)).toSeq.sortBy(_.date).reverse
  val itemsPerPage: Int = appConfig.numberOfItemsPerPage
  val requestedPage: Int = pageNumber.getOrElse(1)

  override val urlForPage: Int => String = e => routes.GuaranteeAccountController.showAccountDetails(Some(e)).url
}

