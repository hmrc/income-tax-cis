/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import config.AppConfig
import models.get.AllCISDeductions
import models.tasklist._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommonTaskListService @Inject()(appConfig: AppConfig,
                                      cisDeductionsService: CISDeductionsService
                                     ) {

  def get(taxYear: Int, nino: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TaskListSection] = {

    val cis: Future[AllCISDeductions] = cisDeductionsService.getCISDeductions(nino, taxYear).map {
      case Left(_) => AllCISDeductions(None, None)
      case Right(value) => value
    }

    cis.map { c =>
      val tasks: Option[Seq[TaskListSectionItem]] = {
        val optionalTasks: Seq[TaskListSectionItem] = getTasks(c, taxYear)
        if (optionalTasks.nonEmpty) {
          Some(optionalTasks)
        } else {
          None
        }
      }
      TaskListSection(SectionTitle.CISTitle, tasks)
    }
  }

  private def getTasks(cisDeductions: AllCISDeductions, taxYear: Int): Seq[TaskListSectionItem] = {

    // TODO: these will be links to the new individual CYA pages when they are made
    val cisCustomerUrl: String =
      s"${appConfig.cisFrontendBaseUrl}/update-and-submit-income-tax-return/construction-industry-scheme-deductions/$taxYear/check-construction-industry-scheme-deductions"

    val cisCustomerDeductions: Option[TaskListSectionItem] = cisDeductions.customerCISDeductions.map(_ =>
      TaskListSectionItem(TaskTitle.cisDeductions, TaskStatus.Completed, Some(cisCustomerUrl)))

    // TODO: waiting on more information regarding Contractor Data
    val cisContractorDeductions: Option[TaskListSectionItem] = cisDeductions.contractorCISDeductions.map(_ =>
      TaskListSectionItem(TaskTitle.cisDeductions, TaskStatus.Completed, Some(cisCustomerUrl)))

    Seq[Option[TaskListSectionItem]](cisCustomerDeductions).flatten
  }
}
