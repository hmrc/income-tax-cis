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
import models.get.{AllCISDeductions, CISSource}
import models.tasklist.SectionTitle.SelfEmploymentTitle
import models.tasklist.TaskTitle.CIS
import models.tasklist._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommonTaskListService @Inject()(appConfig: AppConfig,
                                      cisDeductionsService: CISDeductionsService
                                     ) {

  def get(taxYear: Int, nino: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TaskListSection] = {
    cisDeductionsService.getCISDeductions(nino, taxYear).map {
      case Left(_) => AllCISDeductions(None, None)
      case Right(cis) => cis
    }.map { cis =>
      val tasks: Option[Seq[TaskListSectionItem]] = getTasks(cis, taxYear)
      TaskListSection(SelfEmploymentTitle, tasks)
    }
  }

  private def getTasks(cisDeductions: AllCISDeductions, taxYear: Int): Option[Seq[TaskListSectionItem]] = {

    val cisCustomerUrl: String =
      s"${appConfig.cisFrontendBaseUrl}/update-and-submit-income-tax-return/construction-industry-scheme-deductions/$taxYear/check-construction-industry-scheme-deductions"

    def getSubmissionDate(cisSource: Option[CISSource]): Long = {
      cisSource.flatMap(_
        .cisDeductions.headOption.flatMap(_.periodData
          .map(_.submissionDate).maxOption
          .map(Instant.parse(_).getEpochSecond)
        )).getOrElse(0)
    }

    val customerSubmittedOn: Long = getSubmissionDate(cisDeductions.customerCISDeductions)

    val hmrcSubmittedOn: Long = getSubmissionDate(cisDeductions.contractorCISDeductions)

    val hasCustomerData: Boolean = cisDeductions.customerCISDeductions.exists(_.cisDeductions.nonEmpty)

    val hasHMRCData: Boolean = cisDeductions.contractorCISDeductions.exists(_.cisDeductions.nonEmpty)

    (hmrcSubmittedOn >= customerSubmittedOn && hasHMRCData, hasCustomerData) match {
      case (true, _) => Some(Seq(TaskListSectionItem(CIS, TaskStatus.CheckNow, Some(cisCustomerUrl))))
      case (false, true) => Some(Seq(TaskListSectionItem(CIS, TaskStatus.Completed, Some(cisCustomerUrl))))
      case (_, _) => None
    }
  }
}
